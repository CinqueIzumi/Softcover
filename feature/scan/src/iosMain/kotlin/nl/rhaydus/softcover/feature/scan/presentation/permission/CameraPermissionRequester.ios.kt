package nl.rhaydus.softcover.feature.scan.presentation.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// Barcode scanning isn't implemented on iOS yet (see the core:designsystem BarcodeScanner iOS stub).
// Reporting no camera makes the scan screen open straight into manual ISBN entry, and the requester
// below never grants — so the camera path is never reached.

@Composable
actual fun isCameraAvailable(): Boolean = false

@Composable
actual fun isCameraPermissionGranted(): Boolean = false

@Composable
actual fun rememberCameraPermissionRequester(
    onResult: (Boolean) -> Unit,
): CameraPermissionRequester = remember(onResult) {
    CameraPermissionRequester(
        launchRequest = {},
        isAlreadyGranted = false,
        onResult = onResult,
    )
}
