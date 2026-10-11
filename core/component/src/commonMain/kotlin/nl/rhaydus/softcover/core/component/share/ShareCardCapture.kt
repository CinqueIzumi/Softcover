package nl.rhaydus.softcover.core.component.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.layer.GraphicsLayer
import nl.rhaydus.designsystem.share.ShareCardCapture as FoundationShareCardCapture
import nl.rhaydus.designsystem.share.ShareCardCaptureConfig

/**
 * Captures a card rendered through [ScaledShareCardPreview] to a bitmap and then saves or shares it.
 * Mirrors the foundation capture but reports a dismissed save dialog as [SaveOutcome.Cancelled] and a
 * desktop clipboard write as [ShareOutcome.CopiedToClipboard]. Obtain an instance with
 * [rememberShareCardCapture].
 */
interface ShareCardCapture {
    val graphicsLayer: GraphicsLayer

    /** The foundation capture sharing [graphicsLayer], which `CapturableShareCard` records into. */
    val foundation: FoundationShareCardCapture

    suspend fun saveToGallery(displayName: String): SaveOutcome

    suspend fun saveToCache(displayName: String): SaveOutcome

    suspend fun share(displayName: String): ShareOutcome
}

@Composable
expect fun rememberShareCardCapture(config: ShareCardCaptureConfig): ShareCardCapture
