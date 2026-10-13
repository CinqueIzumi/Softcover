package nl.rhaydus.softcover.core.presentation.error

import nl.rhaydus.softcover.core.domain.exception.InvalidTokenException
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.exception.ServerUnavailableException
import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException

/**
 * The single place API-failure copy is authored: the network seam throws the typed `ApiException`
 * kinds, and presentation maps the kind to a message here. Returns null when nothing should be shown:
 * [InvalidTokenException] is handled by the re-auth dialog (via `SessionExpiredNotifier`), and a
 * non-API throwable (a local DataStore failure, or a bug) is logged but not surfaced.
 */
fun Throwable.toUserMessage(): String? = when (this) {
    is OfflineException -> "You're offline. Check your connection and try again."

    is ServerUnavailableException -> "The server is unavailable right now. Please try again."

    is InvalidTokenException -> null

    is UnexpectedApiException -> "Something went wrong. Please try again."

    else -> null
}
