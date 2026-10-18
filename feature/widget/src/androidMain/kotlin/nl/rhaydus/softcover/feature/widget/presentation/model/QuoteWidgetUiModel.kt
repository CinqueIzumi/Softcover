package nl.rhaydus.softcover.feature.widget.presentation.model

import android.net.Uri
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration

internal data class QuoteWidgetUiModel(
    val themeConfiguration: ThemeConfiguration,
    val content: Content,
) {
    sealed interface Content {
        data object Loading : Content

        data object SignedOut : Content

        data object Empty : Content

        /** A fetch failed and there is nothing cached to show. */
        data object FetchFailed : Content

        /**
         * [eyebrow] is "QUOTE OF THE DAY" or "QUOTES · 2/5"; [byline] is `null` when the quote has no edition title,
         * no book title and no position.
         */
        data class Shown(
            val quoteText: String,
            val byline: String?,
            val bookId: Int,
            val eyebrow: String,
            val hasArrows: Boolean,
            val artwork: Artwork?,
        ) : Content
    }

    /** [backdrop] is `null` while the quote's edition has no cover. */
    data class Artwork(
        val backdrop: Uri?,
        val veil: Uri,
        val veilLift: Uri,
        val washPrimary: Uri,
        val washTertiary: Uri,
    )
}
