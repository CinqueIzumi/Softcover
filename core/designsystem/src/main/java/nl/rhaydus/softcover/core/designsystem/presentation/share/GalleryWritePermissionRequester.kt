package nl.rhaydus.softcover.core.designsystem.presentation.share

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Compose-aware gate for the legacy `WRITE_EXTERNAL_STORAGE` permission needed when writing to
 * the gallery on API ≤ 28. On API 29+ the permission is unnecessary (scoped storage handles it),
 * so [request] fires [onResult] synchronously with `granted = true`.
 */
class GalleryWritePermissionRequester internal constructor(
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

@Composable
fun rememberGalleryWritePermissionRequester(
    onResult: (Boolean) -> Unit,
): GalleryWritePermissionRequester {
    val context = LocalContext.current

    val isScopedStorageEra = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    val isAlreadyGranted = isScopedStorageEra || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    ) == PackageManager.PERMISSION_GRANTED

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult,
    )

    return remember(launcher, isAlreadyGranted) {
        GalleryWritePermissionRequester(
            launchRequest = { launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE) },
            isAlreadyGranted = isAlreadyGranted,
            onResult = onResult,
        )
    }
}
