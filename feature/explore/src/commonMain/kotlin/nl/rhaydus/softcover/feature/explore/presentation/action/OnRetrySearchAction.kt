package nl.rhaydus.softcover.feature.explore.presentation.action

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

        scope.setLocalVariables { it.copy(queryJob = null) }

        scope.setState {
            it.copy(
                isLoading = true,
                searchError = null,
            )
        }

        executeBookSearch(
            name = scope.currentState.searchText,
            dependencies = dependencies,
            scope = scope,
        )
    }
}
