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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesMenuSheetUiModel

private val continueSeriesListState = LazyListState()

@Composable
internal fun ContinueSeriesSection(
    set: BookCardSet<Book>,
    menuSheets: ImmutableMap<Int, ContinueSeriesMenuSheetUiModel>,
    header: SectionHeaderUiModel,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    if (set.isEmpty) return

    // Stamped on mount - see TrendingSection's comment for why.
    val entry = rememberStaggeredEntryCoordinator(key = "explore:continue_series")

    UpNextCardsHost(
        set = set,
        menuSheets = menuSheets,
        onBookClick = onBookClick,
        runAction = runAction,
    ) { onCardEvent ->
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
                itemsIndexed(set.cards, key = { _, card -> card.key.lazyKey }) { index, card ->
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
    }
}
