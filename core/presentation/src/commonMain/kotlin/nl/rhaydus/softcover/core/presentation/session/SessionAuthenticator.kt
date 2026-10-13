package nl.rhaydus.softcover.core.presentation.session

/**
 * Minimal cross-tier seam for features that need to flip the app-level authenticated flag (onboarding
 * raises it on a fresh login; profile drops it on logout) without depending on the app-level
 * `MainActivityViewModel`, which lives in `:orchestration`. The orchestration composition root binds
 * the view model to this contract — mirroring the `AppNavigator` seam.
 */
interface SessionAuthenticator {
    fun setUserAuthenticated(authenticated: Boolean)
}
