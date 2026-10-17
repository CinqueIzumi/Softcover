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
         * While the images are still being stored [cover] and [artwork] are `null` and [progress] is empty.
         */
        data class Reading(
            val title: String,
            val author: String,
            val bookId: Int,
            val progressLabel: String,
            val cover: Uri?,
            /** One render per bar width in dp, keyed by [progressBarWidthDp] of each responsive size bucket. */
            val progress: Map<Int, WavyProgressUris>,
            val eyebrow: String,
            val canCycle: Boolean,
            val artwork: Artwork?,
        ) : Content
    }

    /** [backdrop] is `null` while the book has no cover. */
    data class Artwork(
        val backdrop: Uri?,
        val veil: Uri,
        val veilLift: Uri,
        val washPrimary: Uri,
        val washTertiary: Uri,
    )
}
