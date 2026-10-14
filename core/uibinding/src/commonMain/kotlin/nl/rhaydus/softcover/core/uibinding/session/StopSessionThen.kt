package nl.rhaydus.softcover.core.uibinding.session

import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController

/**
 * Stops the session before running [pause]. A failure from `stop()` is not guarded and propagates
 * without [pause] running.
 */
suspend fun ActiveSessionController.stopSessionThen(pause: suspend () -> Unit) {
    stop()

    pause()
}
