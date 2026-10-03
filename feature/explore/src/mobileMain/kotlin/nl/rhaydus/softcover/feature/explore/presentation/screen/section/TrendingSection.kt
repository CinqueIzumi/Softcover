package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_TRENDING

private val trendingListState = LazyListState()

private val TRENDING_CARD_WIDTH = 118.dp

@Composable
internal fun TrendingSection(
    books: List<Book>,
    cards: ImmutableMap<Int, BookCardUiModel>,
    isLoading: Boolean,
    header: SectionHeaderUiModel,
    onBookClick: (Book, String?) -> Unit,
) {
    if (isLoading.not() && books.isEmpty()) return

    // Hoisted above the crossfade so the coordinator's first-composition timestamp is stamped as
    // soon as this section mounts - typically while still loading - not at the moment content
    // arrives. By the time a real network fetch resolves, the stagger window has usually already
    // elapsed, so the item entrance defers entirely to the crossfade below instead of double-
    // animating alongside it; the stagger only plays when content is already on hand at the
    // section's very first paint (design-system.md §2.5 "Staggered entry on first composition").
    val entry = rememberStaggeredEntryCoordinator(key = "explore:trending")

    val onCardEvent = rememberRailCardEventHandler(
        books = books,
        surface = SURFACE_TRENDING,
        onBookClick = onBookClick,
    )

    // The cards are derived off the books, so they land a frame after them.
    val cardsReady = books.all { it.id in cards }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(
            model = header,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        SkeletonCrossfade(
            isLoading = isLoading || cardsReady.not(),
            label = "TrendingRail",
        ) { loading ->
            if (loading) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(3) {
                        BookCard(
                            model = TRENDING_STAND_IN_CARD,
                            onEvent = {},
                            isLoading = true,
                            modifier = Modifier.width(TRENDING_CARD_WIDTH),
                        )
                    }
                }
            } else {
                LazyRow(
                    state = trendingListState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(books, key = { _, book -> book.id }) { index, book ->
                        cards[book.id]?.let { card ->
                            BookCard(
                                model = card,
                                onEvent = onCardEvent,
                                modifier = Modifier
                                    .width(TRENDING_CARD_WIDTH)
                                    .staggeredEntry(coordinator = entry, index = index),
                            )
                        }
                    }
                }
            }
        }
    }
}
