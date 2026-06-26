package nl.rhaydus.softcover.core.domain.message

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Compose-free channel signalling that the stored auth token was rejected by the server (a 401/403),
 * so the app should prompt the user to re-authenticate without wiping local data. Lives in domain so
 * the network helpers can emit it without depending on presentation; the app root collects [events]
 * and shows the re-auth dialog.
 */
object SessionExpiredNotifier {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
    val events: Flow<Unit> = _events.asSharedFlow()

    fun notifySessionExpired() {
        _events.tryEmit(Unit)
    }
}
