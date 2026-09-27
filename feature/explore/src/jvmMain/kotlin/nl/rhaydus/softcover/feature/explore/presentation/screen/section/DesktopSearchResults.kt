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
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnLoadMoreSearchResultsAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.SEARCH_RESULTS_LOAD_MORE_THRESHOLD
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

private val searchResultsGridState = LazyGridState()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DesktopSearchResults(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            SortChip(
                sortMode = state.sortMode,
                runAction = runAction,
            )
        }

        val subtitle = when {
            state.searchText.isNotEmpty() -> "for \"${state.searchText}\""
            state.activeMoodFilter != null -> "for \"${state.activeMoodFilter.label}\""
            else -> null
        }

        SearchResultsHeader(
            resultCount = state.queriedBooks.size,
            subtitle = subtitle,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback item 7: same append-on-scroll-near-end trigger as mobile's LazyColumn, driven off
        // this grid's own `searchResultsGridState` instead of a per-composition list state.
        LaunchedEffect(state.queriedBooks.size, state.queriedBooksHasMore) {
            val itemCount = state.queriedBooks.size
            if (state.queriedBooksHasMore.not() || itemCount == 0) return@LaunchedEffect

            snapshotFlow { searchResultsGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastVisibleIndex ->
                    val nearEnd = lastVisibleIndex != null &&
                        lastVisibleIndex >= itemCount - SEARCH_RESULTS_LOAD_MORE_THRESHOLD

                    if (nearEnd) {
                        runAction(OnLoadMoreSearchResultsAction)
                    }
                }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            LazyVerticalGrid(
                state = searchResultsGridState,
                columns = GridCells.Adaptive(minSize = 380.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = rememberBottomBarPadding()),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(state.queriedBooks, key = { it.id }) { book ->
                    val cover = state.queriedBookCovers[book.id]

                    SearchResultRow(
                        book = book,
                        cover = cover,
                        onBookClick = onBookClick,
                        runAction = runAction,
                    )
                }

                if (state.loadingMoreQueriedBooks) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularWavyProgressIndicator()
                        }
                    }
                }
            }

            DesktopVerticalScrollbar(
                gridState = searchResultsGridState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
            )
        }
    }
}
