package nl.rhaydus.softcover.core.network.helper

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.apollo.exception.CacheMissException
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import nl.rhaydus.platform.NetworkAvailability
import nl.rhaydus.softcover.core.domain.exception.InvalidTokenException
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.exception.RetryableSyncException
import nl.rhaydus.softcover.core.domain.exception.ServerUnavailableException
import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException
import nl.rhaydus.softcover.core.domain.message.SessionExpiredNotifier

/**
 * True when [statusCode] reflects the server failing to process the request rather than rejecting it:
 * any 5xx, plus 408 (request timeout) and 429 (too many requests). These are transient and worth
 * retrying; a 4xx means the server understood and refused the request, so retrying is pointless.
 */
private fun isTransientHttpStatus(statusCode: Int): Boolean =
    statusCode in 500..599 || statusCode == 408 || statusCode == 429

/**
 * Maps a transport-level Apollo failure to a [RetryableSyncException] when it is transient — the
 * request never got a processable response (offline, an online network failure, or a 5xx/throttle).
 * Returns null for everything else (4xx, parse errors, …): the server processed and rejected the
 * request, so replaying it would fail identically and the caller should surface a genuine error.
 *
 * Shared by [requireData] and [safeQueryFlow] so both paths classify failures identically.
 */
private fun retryableTransportFailureOrNull(exception: Throwable): RetryableSyncException? = when {
    exception is ApolloNetworkException && NetworkAvailability.isOnline().not() -> OfflineException()

    exception is ApolloNetworkException ->
        ServerUnavailableException(
            "Server unreachable: ${exception.message}",
            exception,
        )

    exception is ApolloHttpException && isTransientHttpStatus(exception.statusCode) ->
        ServerUnavailableException(
            "Server error ${exception.statusCode}: ${exception.message}",
            exception,
        )

    else -> null
}

/**
 * Maps an HTTP 401/403 to an [InvalidTokenException] — the server rejected the credentials, so the
 * stored token is bad and replaying is pointless. Returns null for everything else. Emitting on this
 * lets the app prompt a non-destructive re-auth instead of a generic error.
 */
private fun authFailureOrNull(exception: Throwable): InvalidTokenException? =
    if (exception is ApolloHttpException && (exception.statusCode == 401 || exception.statusCode == 403)) {
        InvalidTokenException("Auth rejected (${exception.statusCode})")
    } else {
        null
    }

// Each throw is a distinct, deliberate failure path mapped to the sealed `ApiException` hierarchy
// (retryable transport, auth-rejected, and unexpected/GraphQL), so the count is over the threshold by
// design. Suppressed rather than gated.
@Suppress("ThrowsCount")
private fun <T : Operation.Data> requireData(response: ApolloResponse<T>): T {
    response.exception?.let { exception ->
        // A transient transport/server failure is retryable: the write is applied optimistically and
        // will sync later. Everything else is a genuine failure.
        retryableTransportFailureOrNull(exception)?.let { throw it }

        // A rejected token drives the re-auth dialog rather than a generic error.
        authFailureOrNull(exception)?.let {
            SessionExpiredNotifier.notifySessionExpired()

            throw it
        }

        throw UnexpectedApiException(
            "Apollo error: ${exception.message}",
            exception,
        )
    }

    if (response.errors.isNullOrEmpty().not()) {
        val message = response.errors?.joinToString { error ->
            buildString {
                append(error.message)

                error.path?.let { path ->
                    append(" | path=$path")
                }

                error.extensions?.let { append(" | extensions=$it") }
            }
        } ?: ""

        throw UnexpectedApiException("Apollo GraphQL error(s): \n$message")
    }

    return response.data ?: throw UnexpectedApiException("Apollo response had no data and no errors")
}

private suspend fun <T : Operation.Data> executeCall(
    requireNetwork: Boolean,
    call: suspend () -> ApolloResponse<T>,
): T {
    if (requireNetwork && NetworkAvailability.isOnline().not()) {
        throw OfflineException()
    }

    return requireData(call())
}

suspend fun <T : Mutation.Data> ApolloClient.safeMutation(mutation: Mutation<T>): T =
    executeCall(requireNetwork = true) { this.mutation(mutation).execute() }

suspend fun <T : Query.Data> ApolloClient.safeQuery(
    query: Query<T>,
    fetchPolicy: FetchPolicy = FetchPolicy.NetworkOnly,
): T = executeCall(requireNetwork = fetchPolicy == FetchPolicy.NetworkOnly) {
    // Apollo Kotlin v4's CacheFirst/NetworkFirst interceptors emit TWO responses
    // on the failure path (the failed-stage response, then the fallback). Calling
    // .execute() surfaces the first emission, so a cache miss reaches us as a
    // CacheMissException even though the network leg would have succeeded.
    // Consume the flow and pick the last data-bearing response; fall back to the
    // last emission so genuine errors (offline, GraphQL errors) still propagate.
    val responses = this.query(query).fetchPolicy(fetchPolicy).toFlow().toList()

    responses.lastOrNull { it.data != null && it.errors.isNullOrEmpty() }
        ?: responses.last()
}

internal fun <T : Query.Data> ApolloClient.safeQueryFlow(
    query: Query<T>,
    fetchPolicy: FetchPolicy = FetchPolicy.CacheAndNetwork,
): Flow<T> = flow {
    var lastFailure: Throwable? = null
    var emittedAny = false

    this@safeQueryFlow.query(query).fetchPolicy(fetchPolicy).toFlow().collect { response ->
        val data = response.data

        if (data != null && response.errors.isNullOrEmpty()) {
            emit(data)
            emittedAny = true
        } else {
            response.exception?.let { exception ->
                if (exception !is CacheMissException) {
                    lastFailure = exception
                }
            }
        }
    }

    val failure = lastFailure

    // Surface a rejected token regardless of whether cached data was already emitted — a
    // CacheAndNetwork hit must still raise re-auth when the network leg returns 401/403. Only throw
    // when nothing was emitted; otherwise the cached data stands and we just signal re-auth.
    failure?.let { authFailureOrNull(it) }?.let { invalid ->
        SessionExpiredNotifier.notifySessionExpired()

        if (emittedAny.not()) throw invalid

        return@flow
    }

    if (emittedAny.not()) {
        if (failure != null) {
            retryableTransportFailureOrNull(failure)?.let { throw it }
        }

        throw UnexpectedApiException(
            message = "Apollo flow failed: ${failure?.message ?: "completed with no data"}",
            cause = failure,
        )
    }
}
