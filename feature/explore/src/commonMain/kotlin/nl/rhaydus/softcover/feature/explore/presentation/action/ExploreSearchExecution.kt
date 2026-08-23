package nl.rhaydus.softcover.feature.explore.presentation.action

import kotlinx.coroutines.cancelAndJoin
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.presentation.error.toUserMessage
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Runs whichever search is currently active - a mood filter, if [ExploreScreenUiState.activeMoodFilter]
 * is set, otherwise a text search over [ExploreScreenUiState.searchText] - and folds the result
 * into state. Shared by every action that starts, retries, re-sorts, or pages a search, so the
 * fetch/fold logic lives in exactly one place. [page] is 1-based: 1 is a fresh search/replace,
 * anything higher appends (see `SearchRemoteDataSource`'s paging contract).
 */
internal suspend fun executeSearch(
    dependencies: ExploreDependencies,
    scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    page: Int = 1,
) {
    val state = scope.currentState
    val mood = state.activeMoodFilter

    val result = if (mood != null) {
        dependencies.searchByMoodUseCase(
            mood = mood,
            page = page,
        )
    } else {
        dependencies.searchForNameUseCase(
            name = state.searchText,
            sortMode = state.sortMode,
            page = page,
        )
    }

    result
        .onSuccess {
            scope.setState {
                it.copy(
                    isLoading = false,
                    loadingMoreQueriedBooks = false,
                )
            }
        }
        .onFailure { error ->
            scope.setState {
                it.copy(
                    isLoading = false,
                    loadingMoreQueriedBooks = false,
                    searchError = error.toUserMessage() ?: "We couldn't load results. Please try again.",
                )
            }
        }
}

/**
 * Takes the screen back to "no search running": cancels the in-flight fetch, rewinds the paging
 * cursor, and drops the query, the mood browse, and the results. Shared by the empty-query branch
 * of `OnQueryChangeAction` (the user deleted their last character) and by `OnClearSearchAction`
 * (the user tapped ×) so the two can never rot into different definitions of "cleared".
 *
 * The search chrome's own state - [ExploreScreenUiState.searchFocused] - is deliberately *not*
 * touched: whether the field keeps the user's cursor after a reset is the caller's call, and the
 * two callers answer it differently.
 */
internal suspend fun resetSearch(
    dependencies: ExploreDependencies,
    scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
) {
    scope.currentLocalVariables.queryJob?.cancelAndJoin()

    scope.setLocalVariables {
        it.copy(
            queryJob = null,
            searchResultsPage = 1,
        )
    }

    scope.setState {
        it.copy(
            searchText = "",
            queriedBooks = emptyList(),
            activeMoodFilter = null,
            queriedBooksHasMore = true,
            isLoading = false,
            loadingMoreQueriedBooks = false,
            searchError = null,
        )
    }

    // Also resets the data source's own queried-books state, not just the UiState mirror above -
    // otherwise re-running an identical search later (e.g. re-tapping the same mood) can recompute
    // an equal list that a StateFlow dedups, and the results silently never reach the UI.
    dependencies.clearSearchResultsUseCase().onFailure {
        AppLog.e("$it")
    }
}
