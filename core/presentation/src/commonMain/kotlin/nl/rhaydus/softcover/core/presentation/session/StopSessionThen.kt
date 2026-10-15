package nl.rhaydus.softcover.core.presentation.session

/**
 * Stops the session before running [pause]. A failure from `stop()` is not guarded and propagates
 * without [pause] running.
 */
suspend fun ActiveSessionController.stopSessionThen(pause: suspend () -> Unit) {
    stop()

    pause()
}
