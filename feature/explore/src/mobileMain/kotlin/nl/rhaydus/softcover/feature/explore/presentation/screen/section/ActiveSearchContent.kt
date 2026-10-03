package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.InlineErrorState
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnAddBookToLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnLoadMoreSearchResultsAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveBookFromLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRetrySearchAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.SEARCH_RESULTS_LOAD_MORE_THRESHOLD
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        if (state.searchError != null) {
            InlineErrorState(
                message = state.searchError,
                onRetry = { runAction(OnRetrySearchAction) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                textStyle = MaterialTheme.editorialTypography.bodySmall,
            )

            return
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            SortChip(
                sortMode = state.sortMode,
                runAction = runAction,
            )
        }

        val resultsListState = rememberLazyListState()

        // Feedback item 7: append the next page once the visible window nears the fetched end.
        // There's no server-side total (Typesense returns no hit count), so `queriedBooksHasMore`
        // is the only stop condition — the action itself is the re-entrancy guard (a no-op while a
        // fetch is already in flight), so this can dispatch freely on every qualifying scroll frame.
        LaunchedEffect(resultsListState, state.queriedBooks.size, state.queriedBooksHasMore) {
            val itemCount = state.queriedBooks.size
            if (state.queriedBooksHasMore.not() || itemCount == 0) return@LaunchedEffect

            snapshotFlow { resultsListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastVisibleIndex ->
                    val nearEnd = lastVisibleIndex != null &&
                        lastVisibleIndex >= itemCount - SEARCH_RESULTS_LOAD_MORE_THRESHOLD

                    if (nearEnd) {
                        runAction(OnLoadMoreSearchResultsAction)
                    }
                }
        }

        val booksByKey = remember(state.queriedBooks) {
            state.queriedBooks.associateBy { BookCardKey.Book(it.id) }
        }

        val onSearchResultEvent = rememberPrefetchingBookCardEventHandler(
            resolve = booksByKey::get,
        ) { event, book ->
            when (event) {
                is BookCardEvent.Clicked -> onBookClick(
                    book,
                    null,
                )

                is BookCardEvent.TrailingClicked -> runAction(
                    if (book.userBook != null) {
                        OnRemoveBookFromLibraryClickAction(book = book)
                    } else {
                        OnAddBookToLibraryClickAction(book = book)
                    },
                )

                is BookCardEvent.LongClicked,
                is BookCardEvent.PressStarted,
                is BookCardEvent.MenuClicked,
                is BookCardEvent.ShakeEnded,
                    -> Unit
            }
        }

        val cards = state.queriedBookCards

        if (cards == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = rememberBottomBarPadding()),
            ) {
                items(6) {
                    BookCard(
                        model = SEARCH_RESULT_STAND_IN_CARD,
                        onEvent = {},
                    )
                }
            }
        } else {
            LazyColumn(
                state = resultsListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = rememberBottomBarPadding()),
            ) {
                items(cards, key = { it.key.lazyKey }) { card ->
                    BookCard(
                        model = card,
                        onEvent = onSearchResultEvent,
                    )
                }

                if (state.loadingMoreQueriedBooks) {
                    item {
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
        }
    }
}
