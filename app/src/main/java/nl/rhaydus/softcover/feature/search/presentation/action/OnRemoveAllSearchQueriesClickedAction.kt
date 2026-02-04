package nl.rhaydus.softcover.feature.search.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState
import nl.rhaydus.softcover.feature.search.presentation.screenmodel.SearchDependencies
import timber.log.Timber

class OnRemoveAllSearchQueriesClickedAction() : SearchAction {
    override suspend fun execute(
        dependencies: SearchDependencies,
        scope: ActionScope<SearchScreenUiState, SearchEvent, SearchLocalVariables>,
    ) {
        dependencies.removeAllSearchQueriesUseCase().onFailure {
            Timber.e("-=- $it")
        }
    }
}