package nl.rhaydus.softcover.core.notification

import androidx.compose.runtime.Composable

/**
 * Platform-neutral surface for requesting permission to post notifications.
 *
 * When the permission is already implicitly granted (Android < 33, where it is granted at install
 * time), [request] fires [onResult] synchronously with `granted = true` without showing a dialog;
 * otherwise it delegates to [launchRequest], which surfaces the platform prompt.
 */
class NotificationPermissionRequester internal constructor(
    private val launchRequest: () -> Unit,
    private val isAlreadyGranted: Boolean,
    private val onResult: (Boolean) -> Unit,
) {
    fun request() {
        if (isAlreadyGranted) {
            onResult(true)
        } else {
            launchRequest()
        }
    }
}

/**
 * Remembers a [NotificationPermissionRequester] wired to the current platform's permission prompt.
 * [onResult] is invoked with whether the permission ended up granted.
 */
@Composable
expect fun rememberNotificationPermissionRequester(
    onResult: (Boolean) -> Unit,
): NotificationPermissionRequester
