package nl.rhaydus.softcover.feature.widget.trending

import android.graphics.Bitmap
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration

internal data class TrendingWidgetUiModel(
    val themeConfiguration: ThemeConfiguration,
    val content: Content,
) {
    sealed interface Content {
        data object Loading : Content

        data object SignedOut : Content

        data class Empty(val eyebrow: String) : Content

        /** [pageLabel] is `null` when everything fits on one page. */
        data class Ranked(
            val eyebrow: String,
            val items: ImmutableList<Item>,
            val pageLabel: String?,
            val showsArrows: Boolean,
        ) : Content
    }

    data class Item(
        val bookId: Int,
        val rank: String,
        val title: String,
        val author: String,
        val cover: Bitmap?,
    )
}
