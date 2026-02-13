package nl.rhaydus.softcover.feature.updated_search.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.updated_search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.updated_search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.updated_search.presentation.state.SearchScreenUiState
import nl.rhaydus.softcover.feature.updated_search.presentation.screenmodel.SearchDependencies

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