package nl.rhaydus.softcover.feature.widget.currentlyreading

import android.graphics.Bitmap
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.progress.WavyProgressBitmaps

internal data class CurrentlyReadingWidgetUiModel(
    val themeConfiguration: ThemeConfiguration,
    val content: Content,
) {
    sealed interface Content {
        data object Loading : Content

        data object SignedOut : Content

        data object Empty : Content

        /** [position] is "2/4", and `null` while the shelf holds one book, which also hides the arrows. */
        data class Reading(
            val title: String,
            val author: String,
            val progressLabel: String,
            val cover: Bitmap?,
            val progress: WavyProgressBitmaps,
            val position: String?,
            val artwork: ReadingArtwork? = null,
        ) : Content
    }
}
