package nl.rhaydus.softcover.feature.explore.presentation.action

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Only applies to a text search (mood-filtered results always sort by popularity - see
 * `GetBooksByMoodTag.graphql` - so this is a no-op while [ExploreScreenUiState.activeMoodFilter]
 * is set beyond recording the preference for the next text search).
 */
internal data class OnSortModeChangeAction(val mode: ExploreSortMode) : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        if (scope.currentState.sortMode == mode) return

        scope.setState { it.copy(sortMode = mode) }

        val query = scope.currentState.searchText
        if (query.isEmpty()) return

        scope.currentLocalVariables.queryJob?.cancelAndJoin()

        scope.setLocalVariables {
            it.copy(
                queryJob = null,
                searchResultsPage = 1,
            )
        }

        scope.setState {
            it.copy(
                isLoading = true,
                searchError = null,
                queriedBooksHasMore = true,
            )
        }

        val searchJob: Job = dependencies.launch {
            executeSearch(
                dependencies = dependencies,
                scope = scope,
            )
        }

        scope.setLocalVariables {
            it.copy(queryJob = searchJob)
        }
    }
}
