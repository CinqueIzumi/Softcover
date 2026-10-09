package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

private val searchResultsGridState = LazyGridState()

@Composable
internal fun DesktopSearchResults(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchResultsSection(
            state = state,
            runAction = runAction,
            onBookClick = onBookClick,
        ) { onSearchResultEvent ->
            Row(modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.xs)) {
                SortChip(
                    state = state,
                    runAction = runAction,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            LoadMoreSearchResultsEffect(
                state = state,
                lastVisibleIndex = { searchResultsGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index },
                runAction = runAction,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.gutter),
            ) {
                LazyVerticalGrid(
                    state = searchResultsGridState,
                    columns = GridCells.Adaptive(minSize = 380.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = rememberBottomBarPadding()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    items(state.searchResults.cards, key = { it.key.lazyKey }) { card ->
                        BookCard(
                            model = card,
                            onEvent = onSearchResultEvent,
                        )
                    }

                    if (state.showsLoadingMoreIndicator) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LoadingMoreIndicator()
                        }
                    }
                }

                DesktopVerticalScrollbar(
                    gridState = searchResultsGridState,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(vertical = Spacing.xxs),
                )
            }
        }
    }
}
