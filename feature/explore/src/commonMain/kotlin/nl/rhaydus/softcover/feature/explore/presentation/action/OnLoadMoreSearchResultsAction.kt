package nl.rhaydus.softcover.feature.explore.presentation.action

import kotlinx.coroutines.Job
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Advances whichever search is currently active by one page (explore-3a feedback item 7). A no-op
 * when a fetch is already in flight or the last page came back short, so the UI can dispatch this
 * freely (e.g. on scroll-near-end) without its own re-entrancy guard.
 *
 * The fetch runs in a launched coroutine tracked as [ExploreLocalVariables.queryJob] - the same
 * field every fresh-search action (`OnQueryChangeAction`, `OnMoodChipClickAction`,
 * `OnRetrySearchAction`, `OnSortModeChangeAction`) cancels before it starts. Without that, an
 * in-flight "load more" for the *previous* query/mood could resolve after a fresh search has
 * already replaced [ExploreScreenUiState.queriedBooks] and append its stale results on top of it.
 */
internal data object OnLoadMoreSearchResultsAction : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        val state = scope.currentState
        if (state.isLoading || state.loadingMoreQueriedBooks || state.queriedBooksHasMore.not()) return

        val nextPage = scope.currentLocalVariables.searchResultsPage + 1

        scope.setState { it.copy(loadingMoreQueriedBooks = true) }

        val loadMoreJob: Job = dependencies.launch {
            executeSearch(
                dependencies = dependencies,
                scope = scope,
                page = nextPage,
            )
        }

        scope.setLocalVariables {
            it.copy(
                queryJob = loadMoreJob,
                searchResultsPage = nextPage,
            )
        }
    }
}
