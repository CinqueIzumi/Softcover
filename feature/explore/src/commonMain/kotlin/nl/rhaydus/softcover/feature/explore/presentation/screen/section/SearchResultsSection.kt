package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import nl.rhaydus.designsystem.component.InlineErrorState
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnAddBookToLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnClearSearchAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnLoadMoreSearchResultsAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveBookFromLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRetrySearchAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.SEARCH_RESULTS_LOAD_MORE_THRESHOLD
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

private const val REFRESHING_RESULTS_ALPHA = 0.5f

internal val ExploreScreenUiState.showsSearchStandIns: Boolean
    get() = searchResults.cards.any(BookCardUiModel::isLoading)

internal val ExploreScreenUiState.showsLoadingMoreIndicator: Boolean
    get() = loadingMoreQueriedBooks && showsSearchStandIns.not()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ColumnScope.SearchResultsSection(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
    results: @Composable ColumnScope.(onCardEvent: (BookCardEvent) -> Unit) -> Unit,
) {
    if (state.searchError != null) {
        InlineErrorState(
            message = state.searchError,
            onRetry = { runAction(OnRetrySearchAction) },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.gutter),
            textStyle = MaterialTheme.editorialTypography.bodySmall,
        )

        return
    }

    if (state.showsNoSearchResults) {
        EmptyState(
            model = state.noSearchResultsEmptyState,
            onEvent = { runAction(OnClearSearchAction) },
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = Spacing.gutter,
                    vertical = Spacing.xl,
                ),
        )

        return
    }

    val onSearchResultEvent = rememberPrefetchingBookCardEventHandler(
        set = state.searchResults,
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

    results(onSearchResultEvent)
}

/**
 * Appends the next page once the visible window nears the fetched end. There is no server-side total
 * (Typesense returns no hit count), so `queriedBooksHasMore` is the only stop condition; the action is
 * its own re-entrancy guard (a no-op while a fetch is in flight), so this dispatches freely on every
 * qualifying scroll frame.
 */
@Composable
internal fun LoadMoreSearchResultsEffect(
    state: ExploreScreenUiState,
    lastVisibleIndex: () -> Int?,
    runAction: (ExploreAction) -> Unit,
) {
    val showsStandIns = state.showsSearchStandIns

    LaunchedEffect(state.queriedBooks.size, state.queriedBooksHasMore, showsStandIns) {
        val itemCount = state.queriedBooks.size
        if (state.queriedBooksHasMore.not() || itemCount == 0 || showsStandIns) return@LaunchedEffect

        snapshotFlow(lastVisibleIndex)
            .collect { lastIndex ->
                val nearEnd = lastIndex != null &&
                    lastIndex >= itemCount - SEARCH_RESULTS_LOAD_MORE_THRESHOLD

                if (nearEnd) {
                    runAction(OnLoadMoreSearchResultsAction)
                }
            }
    }
}

/**
 * Keyed on the first real card rather than on `isLoading`: the loading flag can fall before the new
 * results reach state, which would scroll the old list.
 */
@Composable
internal fun ScrollToTopWhenResultsReplacedEffect(
    state: ExploreScreenUiState,
    scrollToTop: suspend () -> Unit,
) {
    val headKey = state.searchResults.cards
        .firstOrNull()
        ?.takeUnless(BookCardUiModel::isLoading)
        ?.key
    var previousHeadKey by remember { mutableStateOf(headKey) }

    LaunchedEffect(headKey) {
        if (headKey != null && previousHeadKey != null && headKey != previousHeadKey) {
            scrollToTop()
        }

        previousHeadKey = headKey
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun Modifier.dimWhileRefreshing(refreshing: Boolean): Modifier {
    val target = if (refreshing) REFRESHING_RESULTS_ALPHA else 1f
    val alpha by animateFloatAsState(
        targetValue = target,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
    )

    return graphicsLayer { this.alpha = alpha }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LazyItemScope.searchResultItemModifier(): Modifier {
    return Modifier.animateItem(
        fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        fadeOutSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LazyGridItemScope.searchResultItemModifier(): Modifier {
    return Modifier.animateItem(
        fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        fadeOutSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LoadingMoreIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        CircularWavyProgressIndicator()
    }
}
