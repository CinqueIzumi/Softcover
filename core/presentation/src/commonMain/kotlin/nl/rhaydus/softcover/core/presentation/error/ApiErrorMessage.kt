package nl.rhaydus.softcover.core.presentation.error

import nl.rhaydus.softcover.core.domain.exception.InvalidTokenException
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.exception.ServerUnavailableException
import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException

/**
 * The single place API-failure copy is authored. Moved out of the network seam (D1) so the data layer
 * no longer decides user-facing strings — the seam throws the typed `ApiException` kinds, and
 * presentation maps the kind to a message here. Returns null when nothing should be shown:
 * [InvalidTokenException] is handled by the re-auth dialog (via `SessionExpiredNotifier`), and a
 * non-API throwable (a local DataStore failure, or a bug) is logged but not surfaced — matching the old
 * seam, which only toasted Apollo failures.
 */
fun Throwable.toUserMessage(): String? = when (this) {
    is OfflineException -> "You're offline. Check your connection and try again."

    is ServerUnavailableException -> "The server is unavailable right now. Please try again."

    is InvalidTokenException -> null

    is UnexpectedApiException -> "Something went wrong. Please try again."

    else -> null
}
