package nl.rhaydus.softcover.core.designsystem.presentation.session

import kotlinx.coroutines.flow.StateFlow
import nl.rhaydus.softcover.core.domain.model.Book

/**
 * App-scoped single source of truth for the active reading session. It exposes the running session
 * (paired with its book) as a [StateFlow] consumed by the peek bar and Focus Mode, and owns every
 * session-control action so all surfaces stay in lockstep. The lock-screen surface is rendered by
 * the foreground service behind the reading-session launcher, which observes [activeSession]; [start]
 * launches it.
 *
 * This is the cross-tier contract: the implementation lives in `:orchestration` (it pulls the
 * reading-session and book use cases), while features depend only on this interface — mirroring the
 * `AppNavigator` seam.
 */
interface ActiveSessionController {
    val activeSession: StateFlow<ActiveSession?>

    /**
     * Set when a lock-screen tap asks to open Focus Mode. Held as a consumable flag (not a one-shot
     * event) so a cold start — where the notification launches the app before the root is collecting
     * — still routes into Focus Mode once the navigator is ready.
     */
    val pendingFocusMode: StateFlow<Boolean>

    fun start(book: Book)

    fun pause()

    fun resume()

    fun stop()

    fun updatePage(newPage: Int)

    fun requestFocusMode()

    fun consumeFocusModeRequest()
}
