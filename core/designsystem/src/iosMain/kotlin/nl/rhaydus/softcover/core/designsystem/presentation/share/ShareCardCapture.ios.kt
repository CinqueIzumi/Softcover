package nl.rhaydus.softcover.core.designsystem.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer

// TODO(iOS): implement share-card capture + save via UIImage/PHPhotoLibrary.
internal class IosShareCardCapture internal constructor(
    override val graphicsLayer: GraphicsLayer,
) : ShareCardCapture {
    override suspend fun saveToGallery(displayName: String): SaveOutcome =
        SaveOutcome.Failure(reason = "gallery save not implemented on iOS")

    override suspend fun saveToCache(displayName: String): SaveOutcome =
        SaveOutcome.Failure(reason = "gallery save not implemented on iOS")
}

@Composable
actual fun rememberShareCardCapture(): ShareCardCapture {
    val graphicsLayer = rememberGraphicsLayer()

    return remember(graphicsLayer) {
        IosShareCardCapture(graphicsLayer = graphicsLayer)
    }
}
