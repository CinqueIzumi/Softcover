package nl.rhaydus.softcover.feature.widget.presentation.model

import android.net.Uri
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
        val cover: Cover,
    )

    sealed interface Cover {
        /** The cover file is still being prepared; render the placeholder. */
        data object Pending : Cover

        data object Missing : Cover

        data class Loaded(val uri: Uri) : Cover
    }
}
