package nl.rhaydus.softcover.feature.widget.presentation.model

import android.graphics.Bitmap
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropArtwork
import nl.rhaydus.softcover.feature.widget.presentation.render.WavyProgressBitmaps

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
            val bookId: Int,
            val progressLabel: String,
            val cover: Bitmap?,
            /** One render per bar width in dp, keyed by [progressBarWidthDp] of each responsive size bucket. */
            val progress: Map<Int, WavyProgressBitmaps>,
            val position: String?,
            val artwork: BackdropArtwork? = null,
        ) : Content
    }
}
