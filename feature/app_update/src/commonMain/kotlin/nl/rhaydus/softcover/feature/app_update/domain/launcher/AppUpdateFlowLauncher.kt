package nl.rhaydus.softcover.feature.app_update.domain.launcher

/**
 * Starts the platform-specific in-app update flow. On Android this drives Google Play's flexible
 * update dialog; platforms without an in-app update mechanism (iOS — updates are an App Store
 * concern) leave it unbound, as nothing requests it there.
 *
 * The launcher is a behavioural seam rather than a typed handle so the AndroidX
 * `ActivityResultLauncher` never crosses into common code: the Android implementation closes over
 * the launcher and the Play `AppUpdateManager` and performs the whole `startUpdateFlowForResult`
 * itself.
 */
fun interface AppUpdateFlowLauncher {
    fun launch()
}
