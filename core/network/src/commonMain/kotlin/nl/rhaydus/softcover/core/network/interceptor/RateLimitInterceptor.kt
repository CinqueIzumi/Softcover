package nl.rhaydus.softcover.core.network.interceptor

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.CompiledField
import com.apollographql.apollo.api.CompiledFragment
import com.apollographql.apollo.api.CompiledSelection
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.apollographql.apollo.network.http.HttpInfo
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import nl.rhaydus.softcover.core.network.helper.HTTP_TOO_MANY_REQUESTS
import nl.rhaydus.softcover.core.network.helper.RateLimitSnapshot
import nl.rhaydus.softcover.core.network.helper.rateLimitSnapshot

// The GraphQL meta field, which the API does not bill as a top-level query.
private const val TYPE_NAME_FIELD = "__typename"

// How much of the reported budget is deliberately left unspent. Grounded in an observed trace: with
// `X-Ratelimit-Limit: 5`, the request the app issued while the server reported one token left came
// back 429, and a later request succeeded while it reported zero. The server's accounting is not a
// plain per-request bucket and is not documented, so the client keeps a token in hand rather than
// trying to spend the budget to its exact edge.
private const val RESERVED_TOKENS = 1.0

// Tokens assumed available before any response has been seen. Deliberately small: on a cold start the
// app knows neither the real burst size nor how much of it a previous session or another device has
// already spent, and the first response replaces this guess with the server's own numbers within a
// few hundred milliseconds. Net of RESERVED_TOKENS this lets exactly one request through before the
// server has said anything — the safe floor, since that first response then unblocks the rest.
private const val UNCONFIRMED_TOKEN_ALLOWANCE = 2.0

/**
 * Paces requests so a burst that would exceed the API's rate limit *waits* rather than being sent and
 * refused. The API returns 429 immediately with no server-side queue, so this is the only place the
 * refusal can be prevented — the callers are a dozen independent coroutines across several feature
 * modules with no knowledge of each other or of the shared budget they draw from.
 *
 * **Closed-loop, not predictive.** The server reports `X-Ratelimit-Limit` / `X-Ratelimit-Remaining` on
 * every response, so this adopts those numbers instead of simulating the server's accounting. An
 * earlier open-loop version modelled a token bucket at the documented rate and still drew 429s,
 * because the real algorithm is neither documented nor a plain per-request bucket: in one captured
 * trace the 5th request was refused at a reported limit of 5 while a 6th succeeded moments later at a
 * reported remaining of 0. Guessing the algorithm is what failed; reading the answer cannot.
 *
 * Local accounting still exists, for the window where the server has not answered yet: [availableTokens]
 * starts at [UNCONFIRMED_TOKEN_ALLOWANCE], is debited per request, replenished at
 * [refillTokensPerSecond], and **overwritten by the server's figure on every response**. A 429 zeroes
 * it and holds every acquisition until `Retry-After` elapses.
 *
 * [intercept] suspends *before* calling [ApolloInterceptorChain.proceed], so a wait delays the request
 * rather than running concurrently with Apollo's HTTP timeout clock. It is registered at
 * `InsertionPoint.BeforeNetwork` so a cache-served request spends nothing.
 *
 * The bucket is charged **per top-level field**, since the API bills each top-level query in a
 * document as one request — `UpdateListBookPositions`, with its aliased `clear` + `apply`, costs two.
 *
 * [fallbackBucketSize] is only the ceiling used until a response supplies the real one; it comes from
 * [ApiRateLimitTier] via `apolloModule`. [timeSource] is injected so tests can drive refill
 * deterministically.
 */
internal class RateLimitInterceptor(
    private val fallbackBucketSize: Int = ApiRateLimitTier.LEGACY_JWT.bucketSize,
    private val refillTokensPerSecond: Double = ApiRateLimitTier.LEGACY_JWT.refillTokensPerSecond,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) : ApolloInterceptor {
    init {
        // A non-positive rate would make the wait computation divide by zero and delay forever.
        // Failing at construction surfaces a wiring mistake immediately instead of as a frozen app.
        require(refillTokensPerSecond > 0.0) { "refillTokensPerSecond must be positive" }
    }

    private val mutex = Mutex()

    private var availableTokens = UNCONFIRMED_TOKEN_ALLOWANCE

    // Requests handed a token and not yet finished. Gates whether a server-reported increase in
    // remaining budget can be trusted — see adoptServerBudget.
    private var inFlight = 0
    private var bucketCeiling = fallbackBucketSize.toDouble()
    private var lastRefillMark = timeSource.markNow()

    // Set when the server returns 429 with a Retry-After; blocks every acquisition until it passes.
    private var holdUntil: TimeMark? = null

    override fun <D : Operation.Data> intercept(
        request: ApolloRequest<D>,
        chain: ApolloInterceptorChain,
    ): Flow<ApolloResponse<D>> = flow {
        acquireTokens(cost = request.operation.rateLimitCost())

        // onStart/onCompletion are paired on the SAME flow so the counter cannot leak. Incrementing
        // outside the pipeline instead would leave `inFlight` permanently high if the coroutine were
        // cancelled before collection began, and a leak here disables upward budget adoption for the
        // rest of the process — the app would silently pace itself at the refill rate forever.
        //
        // BOTH sides run NonCancellable, and the symmetry is the point. `Mutex.withLock` suspends when
        // contended, and that suspension is cancellable: an unprotected increment cancelled while
        // queued for the lock would be skipped, while `onCompletion` — which wraps `onStart` — would
        // still run its protected decrement. `inFlight` would go negative, `inFlight <= 1` would be
        // permanently true, and every stale response would then be trusted to raise the budget. That
        // is the exact failure this class exists to prevent, arriving silently.
        emitAll(
            chain.proceed(request)
                .onStart { withContext(NonCancellable) { mutex.withLock { inFlight++ } } }
                .onEach { response -> adoptServerBudget(response) }
                .onCompletion {
                    withContext(NonCancellable) { mutex.withLock { inFlight-- } }
                },
        )
    }

    /**
     * Replaces the local estimate with the server's own figures. Runs for successful and failed
     * responses alike — a 429 is the most informative response of all, since it states outright that
     * the budget is gone and how long to wait.
     */
    private suspend fun adoptServerBudget(response: ApolloResponse<*>) {
        val snapshot: RateLimitSnapshot = response.rateLimitSnapshot() ?: return
        val throttled: Boolean = response.wasThrottled()

        mutex.withLock {
            // Floored at 1: refill is capped by the ceiling, so a ceiling of 0 — a server bug, or an
            // unrecognised tier reporting nothing usable — would leave the budget unable to reach even
            // a one-token request, and `acquireTokens` would wait forever. Hanging is a worse failure
            // than sending one request too many.
            snapshot.limit?.let { bucketCeiling = it.toDouble().coerceAtLeast(1.0) }

            // A lower figure is always adopted; a higher one only once nothing else is outstanding.
            //
            // Responses do not arrive in the order their requests were sent, so a stale response
            // reporting a healthier budget than a newer one would hand back credit the server has
            // already spent. But refusing every increase is not the answer either: it would leave a
            // cold start pacing itself at the refill rate even while the server reports plenty
            // available, since refill would be the only way up.
            //
            // `inFlight` still counts this request (it is decremented on flow completion), so `<= 1`
            // means this is the last outstanding one — at which point the server's figure accounts for
            // everything sent and can be trusted upwards.
            snapshot.remaining?.let { remaining ->
                val serverTokens = remaining.toDouble()
                val trustIncrease = inFlight <= 1

                availableTokens = if (trustIncrease) {
                    serverTokens
                } else {
                    minOf(
                        availableTokens,
                        serverTokens,
                    )
                }.coerceIn(
                    0.0,
                    bucketCeiling,
                )
            }

            lastRefillMark = timeSource.markNow()

            // Only a refusal establishes a hold. `Retry-After` on a *successful* response is advisory
            // at most, and treating it as a stop-the-world signal would stall the app on a response
            // that actually succeeded.
            if (throttled) {
                availableTokens = 0.0

                snapshot.retryAfter?.let { holdUntil = timeSource.markNow() + it }
            }
        }
    }

    private fun ApolloResponse<*>.wasThrottled(): Boolean {
        val statusCode: Int? = executionContext[HttpInfo]?.statusCode
            ?: (exception as? ApolloHttpException)?.statusCode

        return statusCode == HTTP_TOO_MANY_REQUESTS
    }

    private suspend fun acquireTokens(cost: Int) {
        while (true) {
            // The wait is computed under the lock but performed outside it, so a caller already
            // waiting does not hold up a concurrent caller that has tokens to spend immediately.
            val wait = mutex.withLock { reserveTokensOrWaitDuration(cost = cost) } ?: return

            delay(wait)
        }
    }

    private fun reserveTokensOrWaitDuration(cost: Int): Duration? {
        // A server-instructed hold outranks local accounting: while it stands, no amount of
        // accumulated credit means anything, because the server has said it will refuse.
        holdUntil?.let { mark ->
            if (mark.hasPassedNow()) {
                holdUntil = null
            } else {
                return -mark.elapsedNow()
            }
        }

        refill()

        // Re-clamped against the CURRENT ceiling rather than trusting the value computed when the
        // request arrived. `rateLimitCost()` clamps against whatever the ceiling was then, but a later
        // response can *lower* the ceiling (unlike `remaining`, a reported `limit` is adopted in both
        // directions). Since availableTokens is capped at the ceiling everywhere, a cost left above it
        // could never be satisfied and this loop would spin forever.
        val effectiveCost: Double = cost
            .coerceAtMost(bucketCeiling.toInt().coerceAtLeast(1))
            .toDouble()

        // Spend only down to the reserve, never to zero — see RESERVED_TOKENS. The reserve is dropped
        // when honouring it would make the request unsatisfiable: refill is capped at the ceiling, so
        // demanding more than the ceiling can ever hold would wait forever. A ceiling of 1 with a
        // 1-token request is the realistic case, and hanging there would be far worse than spending
        // the last token.
        val needed = (effectiveCost + RESERVED_TOKENS).coerceAtMost(bucketCeiling.coerceAtLeast(effectiveCost))

        if (availableTokens >= needed) {
            availableTokens -= effectiveCost

            return null
        }

        return ((needed - availableTokens) / refillTokensPerSecond).seconds
    }

    private fun refill() {
        val elapsedSeconds = lastRefillMark.elapsedNow().toDouble(DurationUnit.SECONDS)

        if (elapsedSeconds <= 0.0) return

        availableTokens = (availableTokens + elapsedSeconds * refillTokensPerSecond)
            .coerceAtMost(bucketCeiling)
        lastRefillMark = timeSource.markNow()
    }

    /**
     * How many tokens [operation] costs: one per top-level field in its document, since that is what
     * the API bills.
     *
     * Clamped to at least 1 so no operation is ever free, and to at most the current ceiling so an
     * operation asking for more than the budget can ever hold waits once and proceeds instead of
     * looping forever — the server would reject such a document outright (its own cap is 5 top-level
     * fields), and a hang would be a worse failure than that rejection.
     */
    private fun Operation<*>.rateLimitCost(): Int =
        rootField().selections.countTopLevelFields().coerceIn(
            minimumValue = 1,
            maximumValue = bucketCeiling.toInt().coerceAtLeast(1),
        )

    /**
     * Counts the billable root fields in [this]. `__typename` is excluded: it is a meta field the
     * server does not charge for. Apollo's `addTypename = "always"` only injects it into nested
     * composite selections, not the root, so today this never subtracts anything — it is here so the
     * count stays correct if that ever changes.
     *
     * A root-level fragment spread contributes the fields inside it, which is how the server sees them
     * too. Two fragments selecting the same root field would be counted twice; that over-charges by a
     * token rather than under-charging, which is the safe direction, and no operation here does it.
     */
    private fun List<CompiledSelection>.countTopLevelFields(): Int = sumOf { selection ->
        when (selection) {
            is CompiledField -> if (selection.name == TYPE_NAME_FIELD) 0 else 1
            is CompiledFragment -> selection.selections.countTopLevelFields()
            else -> 0
        }
    }
}
