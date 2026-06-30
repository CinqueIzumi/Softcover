package nl.rhaydus.softcover.core.designsystem.presentation.share

import androidx.compose.runtime.Composable

internal interface GalleryWritePermissionRequester {
    fun request()
}

@Composable
internal expect fun rememberGalleryWritePermissionRequester(
    onResult: (Boolean) -> Unit,
): GalleryWritePermissionRequester
