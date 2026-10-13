package nl.rhaydus.softcover.core.component.share

import androidx.compose.ui.graphics.layer.GraphicsLayer
import nl.rhaydus.designsystem.share.SaveOutcome as FoundationSaveOutcome
import nl.rhaydus.designsystem.share.ShareCardCapture as FoundationShareCardCapture
import nl.rhaydus.designsystem.share.ShareOutcome as FoundationShareOutcome

/** The app capture for platforms whose foundation capture already reports every outcome faithfully. */
internal class DelegatingShareCardCapture(
    override val foundation: FoundationShareCardCapture,
) : ShareCardCapture {
    override val graphicsLayer: GraphicsLayer
        get() = foundation.graphicsLayer

    override suspend fun saveToGallery(displayName: String): SaveOutcome =
        foundation.saveToGallery(displayName).toApp()

    override suspend fun saveToCache(displayName: String): SaveOutcome =
        foundation.saveToCache(displayName).toApp()

    override suspend fun share(displayName: String): ShareOutcome =
        when (val outcome = foundation.share(displayName)) {
            FoundationShareOutcome.Shared -> ShareOutcome.Shared

            FoundationShareOutcome.Cancelled -> ShareOutcome.Cancelled

            is FoundationShareOutcome.Failure -> ShareOutcome.Failure(reason = outcome.reason)
        }
}

internal fun FoundationSaveOutcome.toApp(): SaveOutcome =
    when (this) {
        is FoundationSaveOutcome.Cached -> SaveOutcome.Cached(identifier = identifier)

        is FoundationSaveOutcome.Saved -> SaveOutcome.Saved(
            identifier = identifier,
            displayPath = displayPath,
        )

        is FoundationSaveOutcome.Failure -> SaveOutcome.Failure(reason = reason)
    }
