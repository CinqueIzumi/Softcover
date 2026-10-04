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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_UP_NEXT

private val continueSeriesListState = LazyListState()

@Composable
internal fun ContinueSeriesSection(
    books: List<Book>,
    menuSheets: ImmutableMap<Int, SheetScaffoldUiModel>,
    cards: ImmutableList<BookCardUiModel>,
    isLoading: Boolean,
    header: SectionHeaderUiModel,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    if (isLoading.not() && books.isEmpty()) return

    var sheetBook by remember { mutableStateOf<Book?>(null) }

    // Stamped on mount - see TrendingSection's comment for why.
    val entry = rememberStaggeredEntryCoordinator(key = "explore:continue_series")

    val booksByKey = remember(books) { books.associateBy { BookCardKey.Book(it.id) } }

    val onCardEvent = rememberPrefetchingBookCardEventHandler(
        resolve = booksByKey::get,
    ) { event, book ->
        when (event) {
            is BookCardEvent.Clicked -> onBookClick(
                book,
                SURFACE_UP_NEXT,
            )

            is BookCardEvent.MenuClicked -> sheetBook = book

            is BookCardEvent.LongClicked,
            is BookCardEvent.PressStarted,
            is BookCardEvent.TrailingClicked,
            is BookCardEvent.ShakeEnded,
                -> Unit
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(
            model = header,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        LazyRow(
            state = continueSeriesListState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(cards, key = { _, card -> card.key.lazyKey }) { index, card ->
                BookCard(
                    model = card,
                    onEvent = onCardEvent,
                    modifier = Modifier
                        .width(126.dp)
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

    sheetBook?.let { book ->
        menuSheets[book.id]?.let { menuSheet ->
            ContinueSeriesMenuSheet(
                book = book,
                model = menuSheet,
                runAction = runAction,
                onDismiss = { sheetBook = null },
            )
        }
    }
}
