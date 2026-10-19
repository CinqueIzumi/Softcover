package nl.rhaydus.softcover.feature.widget.presentation.model

import android.net.Uri
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.presentation.render.WavyProgressUris

internal data class CurrentlyReadingWidgetUiModel(
    val themeConfiguration: ThemeConfiguration,
    val content: Content,
) {
    sealed interface Content {
        data object Loading : Content

        data object SignedOut : Content

        data object Empty : Content

        /**
         * [eyebrow] is "READING · 2/4", or "READING" while the shelf holds one book, which also hides the arrows.
         * [imagesPending] is `true` until the image work has finished; meanwhile [cover] and [artwork] are `null` and
         * [progress] is empty.
         */
        data class Reading(
            val title: String,
            val author: String,
            val bookId: Int,
            val progressLabel: String,
            val cover: Uri?,
            /** One render per bar width in dp, keyed by [progressBarWidthDp] of each size the launcher reports. */
            val progress: Map<Int, WavyProgressUris>,
            val eyebrow: String,
            val canCycle: Boolean,
            val artwork: BookArtworkUiModel?,
            val imagesPending: Boolean,
        ) : Content
    }
}
