package nl.rhaydus.softcover.core.network.helper

import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.network.http.HttpInfo
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import nl.rhaydus.common.AppLog

/** HTTP 429, the status the API returns once a rate limit is exhausted. */
internal const val HTTP_TOO_MANY_REQUESTS = 429

private const val HEADER_RETRY_AFTER = "Retry-After"
private const val HEADER_LIMIT = "X-Ratelimit-Limit"
private const val HEADER_REMAINING = "X-Ratelimit-Remaining"

// Conservative hold used when a wait header is present but not delta-seconds; see durationOrNull.
private val UNPARSEABLE_WAIT_FALLBACK = 5.seconds

/**
 * What the API says about the caller's remaining budget, read off a response.
 *
 * The server reports its own rate-limit state on **every** response, which makes it the authority:
 * `X-Ratelimit-Limit` is the burst size for the credentials in use (so the app does not have to guess
 * a tier once a single response has arrived), `X-Ratelimit-Remaining` is what is left of it, and
 * `Retry-After` accompanies a 429. Modelling the server's own accounting client-side instead means
 * predicting a leak/window algorithm that is not documented — and predicting it wrong shows up as a
 * refused request, which is the failure the limiter exists to avoid.
 */
internal data class RateLimitSnapshot(
    val limit: Int?,
    val remaining: Int?,
    val retryAfter: Duration?,
)

/** The snapshot carried by a successful response, via Apollo's [HttpInfo] execution-context element. */
internal fun ApolloResponse<*>.rateLimitSnapshot(): RateLimitSnapshot? {
    val headers: List<HttpHeader> = executionContext[HttpInfo]?.headers
        ?: (exception as? ApolloHttpException)?.headers
        ?: return null

    return headers.toRateLimitSnapshot()
}

/** The snapshot carried by a failed response, where the headers hang off the exception instead. */
internal fun ApolloHttpException.rateLimitSnapshot(): RateLimitSnapshot = headers.toRateLimitSnapshot()

internal fun ApolloHttpException.retryAfterOrNull(): Duration? = headers.durationOrNull(HEADER_RETRY_AFTER)

private fun List<HttpHeader>.toRateLimitSnapshot(): RateLimitSnapshot = RateLimitSnapshot(
    limit = intOrNull(HEADER_LIMIT),
    remaining = intOrNull(HEADER_REMAINING),
    retryAfter = durationOrNull(HEADER_RETRY_AFTER),
)

private fun List<HttpHeader>.valueOrNull(name: String): String? = firstOrNull {
    it.name.equals(
        name,
        ignoreCase = true,
    )
}?.value

private fun List<HttpHeader>.intOrNull(name: String): Int? = valueOrNull(name)?.trim()?.toIntOrNull()

/**
 * Reads a delta-seconds header, falling back to [UNPARSEABLE_WAIT_FALLBACK] when the value is present
 * but not a number.
 *
 * RFC 7231 also permits `Retry-After` to carry an HTTP-date, and the captured traces from this API only
 * ever show the numeric form. Rather than parse dates, an unrecognised value is treated as "the server
 * said wait, for an unknown period" — the alternative, treating it as no instruction at all, retries
 * sooner than asked and invites a second refusal, which is the failure this whole seam exists to avoid.
 */
private fun List<HttpHeader>.durationOrNull(name: String): Duration? {
    val raw: String = valueOrNull(name)?.trim() ?: return null

    raw.toLongOrNull()?.let { return it.seconds }

    AppLog.w("Could not read $name header '$raw' as seconds; holding for $UNPARSEABLE_WAIT_FALLBACK")

    return UNPARSEABLE_WAIT_FALLBACK
}
