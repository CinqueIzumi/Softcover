package nl.rhaydus.softcover.core.notification

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// POST_NOTIFICATIONS is API 33, but the lambda only runs when SDK_INT >= TIRAMISU (guarded via isAlreadyGranted in
// request()); the String constant inlines safely.
@SuppressLint("InlinedApi")
@Composable
actual fun rememberNotificationPermissionRequester(
    onResult: (Boolean) -> Unit,
): NotificationPermissionRequester {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult,
    )

    // On API < 33 the permission is granted at install time, so no dialog is needed.
    val isAlreadyGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU

    return remember(launcher, isAlreadyGranted) {
        NotificationPermissionRequester(
            launchRequest = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
            isAlreadyGranted = isAlreadyGranted,
            onResult = onResult,
        )
    }
}
