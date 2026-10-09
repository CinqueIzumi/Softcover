package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

@Composable
internal fun ActiveSearchContent(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize(),
    ) {
        SearchResultsSection(
            state = state,
            runAction = runAction,
            onBookClick = onBookClick,
        ) { onSearchResultEvent ->
            Row(
                modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
            ) {
                SortChip(
                    state = state,
                    runAction = runAction,
                )
            }

            val resultsListState = rememberLazyListState()

            LoadMoreSearchResultsEffect(
                state = state,
                lastVisibleIndex = { resultsListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index },
                runAction = runAction,
            )

            LazyColumn(
                state = resultsListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = Spacing.gutter),
                contentPadding = PaddingValues(bottom = rememberBottomBarPadding()),
            ) {
                items(state.searchResults.cards, key = { it.key.lazyKey }) { card ->
                    BookCard(
                        model = card,
                        onEvent = onSearchResultEvent,
                    )
                }

                if (state.showsLoadingMoreIndicator) {
                    item {
                        LoadingMoreIndicator()
                    }
                }
            }
        }
    }
}
