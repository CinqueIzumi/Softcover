package nl.rhaydus.softcover.feature.explore.presentation.action

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal data object OnRetrySearchAction : ExploreAction {
    override suspend fun execute(
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
