package nl.rhaydus.softcover.core.designsystem.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer

interface ShareCardCapture {
    val graphicsLayer: GraphicsLayer

    suspend fun saveToGallery(displayName: String): SaveOutcome

    suspend fun saveToCache(displayName: String): SaveOutcome
}

@Composable
expect fun rememberShareCardCapture(): ShareCardCapture

internal fun ContentDrawScope.recordAndDraw(graphicsLayer: GraphicsLayer) {
    graphicsLayer.record { this@recordAndDraw.drawContent() }

    drawLayer(graphicsLayer)
}
