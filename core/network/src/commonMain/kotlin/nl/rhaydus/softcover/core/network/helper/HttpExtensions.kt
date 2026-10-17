package nl.rhaydus.softcover.core.network.helper

import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.network.http.HttpEngine
import com.apollographql.apollo.network.http.get
import nl.rhaydus.platform.NetworkAvailability
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.exception.ServerUnavailableException
import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException

// Apollo's `HttpResponse` carries a bare `statusCode: Int` and no `isSuccessful`-style accessor, so
// the 2xx test has to be written once somewhere. It lives here, beside `isTransientHttpStatus`, so
// the module has exactly one vocabulary for reading an HTTP status.
private val SUCCESSFUL_HTTP_STATUS = 200..299

/**
 * The plain-HTTP sibling of [safeQuery] / [safeMutation]: GETs [url] and returns the body as text.
 * Same shape as the GraphQL seam by design — an offline pre-check, then every failure raised as the
 * sealed `ApiException` model rather than a bare transport exception, so presentation maps it to copy
 * through the one `Throwable.toUserMessage()` table and callers never special-case this seam.
 *
 * Rides Apollo's multiplatform [HttpEngine] because `apollo-runtime` already ships one on every
 * target; a plain-text GET needs no second HTTP stack.
 *
 * The `catch` is the seam's own job, not error-hiding: [HttpEngine.execute] *throws* its transport
 * failure where Apollo's GraphQL path returns it on the response, so this is the same translation
 * `requireData` performs, at the only point the untyped exception exists. `CancellationException` is
 * not an [ApolloException] and so is never caught. Unlike the GraphQL seam there is no 401/403 →
 * re-auth path: this seam sends no credentials, so a rejected status says nothing about the session.
 */
suspend fun HttpEngine.safeGetText(url: String): String {
    if (NetworkAvailability.isOnline().not()) throw OfflineException()

    val response = try {
        get(url).execute()
    } catch (exception: ApolloException) {
        throw retryableTransportFailureOrNull(exception) ?: UnexpectedApiException(
            "HTTP GET failed for $url: ${exception.message}",
            exception,
        )
    }

    if (response.statusCode !in SUCCESSFUL_HTTP_STATUS) {
        throw if (isTransientHttpStatus(response.statusCode)) {
            ServerUnavailableException("HTTP ${response.statusCode} for $url")
        } else {
            UnexpectedApiException("HTTP ${response.statusCode} for $url")
        }
    }

    return response.body?.readUtf8() ?: throw UnexpectedApiException("HTTP response had no body for $url")
}
