package nl.rhaydus.softcover.core.platform.notification

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Composable surface for requesting the runtime POST_NOTIFICATIONS permission on Android 13+.
 *
 * On API < 33 the permission is granted at install time, so [NotificationPermissionRequester.request]
 * fires the [onResult] callback synchronously with `granted = true` without showing a dialog.
 */
class NotificationPermissionRequester internal constructor(
    private val launchRequest: () -> Unit,
    private val isPreTiramisu: Boolean,
    private val onResult: (Boolean) -> Unit,
) {
    fun request() {
        if (isPreTiramisu) {
            onResult(true)
        } else {
            launchRequest()
        }
    }
}

@Composable
fun rememberNotificationPermissionRequester(
    onResult: (Boolean) -> Unit,
): NotificationPermissionRequester {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult,
    )

    val isPreTiramisu = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU

    return remember(launcher, isPreTiramisu) {
        NotificationPermissionRequester(
            launchRequest = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
            isPreTiramisu = isPreTiramisu,
            onResult = onResult,
        )
    }
}
