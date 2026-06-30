package nl.rhaydus.softcover.core.designsystem.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// TODO(iOS): real Photos add-permission once gallery save lands.
internal class IosGalleryWritePermissionRequester internal constructor(
    private val onResult: (Boolean) -> Unit,
) : GalleryWritePermissionRequester {
    override fun request() {
        // Gallery permission is moot until the save itself is implemented; report granted so the
        // caller's pending-save flow proceeds (and then no-ops via the capture stub).
        onResult(true)
    }
}

@Composable
internal actual fun rememberGalleryWritePermissionRequester(
    onResult: (Boolean) -> Unit,
): GalleryWritePermissionRequester =
    remember(onResult) {
        IosGalleryWritePermissionRequester(onResult = onResult)
    }
