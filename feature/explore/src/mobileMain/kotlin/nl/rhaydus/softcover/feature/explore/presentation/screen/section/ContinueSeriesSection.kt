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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesEntryUiModel
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_UP_NEXT

private val continueSeriesListState = LazyListState()

private val UP_NEXT_CARD_WIDTH = 126.dp

@Composable
internal fun ContinueSeriesSection(
    entries: List<ContinueSeriesEntryUiModel>,
    cards: ImmutableMap<Int, BookCardUiModel>,
    isLoading: Boolean,
    header: SectionHeaderUiModel,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    if (isLoading.not() && entries.isEmpty()) return

    var sheetEntry by remember { mutableStateOf<ContinueSeriesEntryUiModel?>(null) }

    // Hoisted above the crossfade - see TrendingSection's comment for why.
    val entry = rememberStaggeredEntryCoordinator(key = "explore:continue_series")

    val books = remember(entries) { entries.map { it.book } }

    val onCardEvent = rememberRailCardEventHandler(
        books = books,
        surface = SURFACE_UP_NEXT,
        onBookClick = onBookClick,
        onMenuClick = { book -> sheetEntry = entries.firstOrNull { it.book.id == book.id } },
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
            label = "ContinueSeriesRail",
        ) { loading ->
            if (loading) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(3) {
                        BookCard(
                            model = UP_NEXT_STAND_IN_CARD,
                            onEvent = {},
                            isLoading = true,
                            modifier = Modifier.width(UP_NEXT_CARD_WIDTH),
                        )
                    }
                }
            } else {
                LazyRow(
                    state = continueSeriesListState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(entries, key = { _, item -> item.book.id }) { index, item ->
                        cards[item.book.id]?.let { card ->
                            BookCard(
                                model = card,
                                onEvent = onCardEvent,
                                modifier = Modifier
                                    .width(UP_NEXT_CARD_WIDTH)
                                    .staggeredEntry(coordinator = entry, index = index),
                            )
                        }
                    }
                }
            }
        }
    }

    sheetEntry?.let { item ->
        ContinueSeriesMenuSheet(
            book = item.book,
            model = item.menuSheet,
            runAction = runAction,
            onDismiss = { sheetEntry = null },
        )
    }
}
