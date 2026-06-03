package nl.rhaydus.softcover.core.designsystem.presentation.session

/**
 * Starts the platform foreground service that renders the live reading-session lock-screen surface.
 * The service itself lives in the `session` feature (it owns the notification chrome); this contract
 * lets the app-scoped [ActiveSessionController] in core launch it without depending on the feature.
 */
interface ReadingSessionLauncher {
    fun start()
}
