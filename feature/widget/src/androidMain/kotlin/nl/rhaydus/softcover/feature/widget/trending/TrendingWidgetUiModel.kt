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

        data object Empty : Content

        data class Ranked(val items: ImmutableList<Item>) : Content
    }

    data class Item(
        val bookId: Int,
        val rank: String,
        val title: String,
        val author: String,
        val cover: Bitmap?,
    )
}
