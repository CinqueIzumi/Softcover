package nl.rhaydus.softcover.core.designsystem.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// Desktop has no runtime gallery permission; report granted so the caller's pending-save flow
// proceeds (and then no-ops via the capture stub until desktop export lands).
internal class JvmGalleryWritePermissionRequester internal constructor(
    private val onResult: (Boolean) -> Unit,
) : GalleryWritePermissionRequester {
    override fun request() {
        onResult(true)
    }
}

@Composable
internal actual fun rememberGalleryWritePermissionRequester(
    onResult: (Boolean) -> Unit,
): GalleryWritePermissionRequester =
    remember(onResult) {
        JvmGalleryWritePermissionRequester(onResult = onResult)
    }
