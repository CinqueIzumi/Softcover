package nl.rhaydus.softcover.feature.scan.presentation.permission

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun isCameraAvailable(): Boolean {
    val context = LocalContext.current

    return remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }
}

@Composable
actual fun isCameraPermissionGranted(): Boolean {
    val context = LocalContext.current

    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
actual fun rememberCameraPermissionRequester(
    onResult: (Boolean) -> Unit,
): CameraPermissionRequester {
    val isAlreadyGranted = isCameraPermissionGranted()

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
