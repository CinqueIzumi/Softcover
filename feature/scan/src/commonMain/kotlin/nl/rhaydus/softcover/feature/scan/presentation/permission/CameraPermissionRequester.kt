package nl.rhaydus.softcover.feature.scan.presentation.permission

import androidx.compose.runtime.Composable

/**
 * Compose-aware gate for the runtime `CAMERA` permission. Unlike the notification/gallery gates
 * there is no install-time shortcut — `CAMERA` is a runtime permission on every supported SDK —
 * so [request] fires [onResult] synchronously only when the permission is already granted, and
 * otherwise launches the system dialog.
 *
 * The type itself is platform-agnostic — it holds only the launch lambda and the already-granted
 * flag. The platform pieces (camera availability, the permission check, and wiring the launch to
 * the system dialog) live behind the [expect] functions below.
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
 * Whether the device exposes camera hardware *and* the platform implements barcode scanning. Seeds
 * the scan screen's initial mode: when this is `false` the screen opens straight into manual ISBN
 * entry. iOS reports `false` until scanning is implemented (see the `core:designsystem`
 * `BarcodeScanner` iOS stub).
 */
@Composable
expect fun isCameraAvailable(): Boolean

/**
 * Synchronous "is the camera permission already granted?" check, used to seed the scan screen's
 * initial UI without a frame of flicker before the requester's callback lands.
 */
@Composable
expect fun isCameraPermissionGranted(): Boolean

/**
 * Remembers a [CameraPermissionRequester] wired to the current platform's runtime-permission
 * prompt; [onResult] is invoked with whether the `CAMERA` permission ended up granted. On platforms
 * that don't implement scanning the requester is a no-op that never grants.
 */
@Composable
expect fun rememberCameraPermissionRequester(
    onResult: (Boolean) -> Unit,
): CameraPermissionRequester
