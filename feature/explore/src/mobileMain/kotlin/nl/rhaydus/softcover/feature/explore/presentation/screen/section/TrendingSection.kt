package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_TRENDING

private val trendingListState = LazyListState()

private val TRENDING_CARD_WIDTH = 118.dp

@Composable
internal fun TrendingSection(
    books: List<Book>,
    cards: ImmutableList<BookCardUiModel>,
    isLoading: Boolean,
    header: SectionHeaderUiModel,
    onBookClick: (Book, String?) -> Unit,
) {
    if (isLoading.not() && books.isEmpty()) return

    // Stamped as soon as this section mounts - typically while still loading - not at the moment
    // content arrives. By the time a real network fetch resolves, the stagger window has usually
    // already elapsed, so the stagger only plays when content is already on hand at the section's
    // very first paint (design-system.md §2.5 "Staggered entry on first composition").
    val entry = rememberStaggeredEntryCoordinator(key = "explore:trending")

    val booksByKey = remember(books) { books.associateBy { BookCardKey.Book(it.id) } }

    val onCardEvent = rememberPrefetchingBookCardEventHandler(
        resolve = booksByKey::get,
    ) { event, book ->
        if (event is BookCardEvent.Clicked) {
            onBookClick(
                book,
                SURFACE_TRENDING,
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(
            model = header,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        LazyRow(
            state = trendingListState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(cards, key = { _, card -> card.key.lazyKey }) { index, card ->
                BookCard(
                    model = card,
                    onEvent = onCardEvent,
                    modifier = Modifier
                        .width(TRENDING_CARD_WIDTH)
                        .let {
                            if (card.isLoading) {
                                it
                            } else {
                                it.staggeredEntry(
                                    coordinator = entry,
                                    index = index,
                                )
                            }
                        },
                )
            }
        }
    }
}
