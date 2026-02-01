package nl.rhaydus.softcover.feature.search.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState
import nl.rhaydus.softcover.feature.search.presentation.viewmodel.SearchDependencies

class PreviousQueriesCollector() : SearchInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<SearchScreenUiState, SearchEvent, SearchLocalVariables>,
        dependencies: SearchDependencies,
    ) {
        dependencies.getPreviousSearchQueriesUseCase().collectLatest { queries ->
            scope.setState {
                it.copy(previousSearchQueries = queries)
            }
        }
    }
}