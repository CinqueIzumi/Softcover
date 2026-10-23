package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * A horizontal rail of [set]'s cards, each [cardWidth] wide; a rail of stand-ins renders as loading cards.
 *
 * @property staggerKey Scopes the entry stagger, so a rail staggers its cards in once per screen visit.
 * @property canLoadMore The caller has a next page to fetch; only then does the rail raise [BookCardRailEvent.NearEnd].
 * @property isAppending A trailing progress card follows the cards while the next page is fetched.
 */
@Immutable
data class BookCardRailUiModel<out P>(
    val set: BookCardSet<P>,
    val cardWidth: Dp,
    val staggerKey: String,
    val isAppending: Boolean = false,
    val canLoadMore: Boolean = false,
) {
    companion object : UiModelPreviews<BookCardRailUiModel<*>> {
        private val LOADED_CARDS = listOf(
            bylineCard(
                bookId = 1,
                title = "The Name of the Wind",
                author = "Patrick Rothfuss",
            ),
            bylineCard(
                bookId = 2,
                title = "Piranesi",
                author = "Susanna Clarke",
            ),
            bylineCard(
                bookId = 3,
                title = "Gideon the Ninth",
                author = "Tamsyn Muir",
            ),
            bylineCard(
                bookId = 4,
                title = "Hyperion",
                author = "Dan Simmons",
            ),
        ).toImmutableList()

        private val LOADING_CARDS = List(3) { index ->
            bylineCard(
                bookId = -(index + 1),
                title = "The Name of the Wind",
                author = "Patrick Rothfuss",
                isLoading = true,
            )
        }.toImmutableList()

        override val previews: ImmutableMap<String, BookCardRailUiModel<*>> = persistentMapOf(
            "Loaded" to BookCardRailUiModel(
                set = BookCardSet.standIns(LOADED_CARDS),
                cardWidth = 118.dp,
                staggerKey = "gallery:rail:loaded",
            ),
            "Loading" to BookCardRailUiModel(
                set = BookCardSet.standIns(LOADING_CARDS),
                cardWidth = 118.dp,
                staggerKey = "gallery:rail:loading",
            ),
            "Appending" to BookCardRailUiModel(
                set = BookCardSet.standIns(LOADED_CARDS),
                cardWidth = 118.dp,
                staggerKey = "gallery:rail:appending",
                isAppending = true,
                canLoadMore = true,
            ),
        )

        private fun bylineCard(
            bookId: Int,
            title: String,
            author: String,
            isLoading: Boolean = false,
        ) = BookCardUiModel(
            key = BookCardKey.Book(bookId = bookId),
            variant = BookCardVariant.Column(
                cover = BookCardCover(
                    model = CoverUiModel(
                        source = CoverSource.Local(
                            path = "/fixtures/cover.jpg",
                            cacheKey = null,
                        ),
                        coverlessTitle = title,
                        variant = CoverVariant.Raised,
                        isLoading = isLoading,
                    ),
                ),
                text = BookCardContent(
                    title = title,
                    byline = "By $author",
                ),
            ),
            isLoading = isLoading,
        )
    }
}
