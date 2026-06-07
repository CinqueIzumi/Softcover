package nl.rhaydus.softcover.feature.scan.presentation.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Compose-aware gate for the runtime `CAMERA` permission. Unlike the notification/gallery gates
 * there is no install-time shortcut — `CAMERA` is a runtime permission on every supported SDK —
 * so [request] fires [onResult] synchronously only when the permission is already granted, and
 * otherwise launches the system dialog.
 */
class CameraPermissionRequester internal constructor(
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
 * Synchronous "is the camera permission already granted?" check, shared between the requester (to
 * decide whether to launch the dialog) and call sites (to seed their initial UI without a frame of
 * flicker before the requester's callback lands).
 */
fun isCameraPermissionGranted(context: Context): Boolean = ContextCompat.checkSelfPermission(
    context,
    Manifest.permission.CAMERA,
) == PackageManager.PERMISSION_GRANTED

@Composable
fun rememberCameraPermissionRequester(
    onResult: (Boolean) -> Unit,
): CameraPermissionRequester {
    val context = LocalContext.current

    val isAlreadyGranted = isCameraPermissionGranted(context = context)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult,
    )

    return remember(launcher, isAlreadyGranted) {
        CameraPermissionRequester(
            launchRequest = { launcher.launch(Manifest.permission.CAMERA) },
            isAlreadyGranted = isAlreadyGranted,
            onResult = onResult,
        )
    }
}
