package nl.rhaydus.softcover.feature.search.presentation.viewmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadViewModel
import nl.rhaydus.softcover.feature.search.domain.usecase.GetPreviousSearchQueriesUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.GetQueriedBooksUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.RemoveAllSearchQueriesUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.RemoveSearchQueryUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.SearchForNameUseCase
import nl.rhaydus.softcover.feature.search.presentation.action.SearchAction
import nl.rhaydus.softcover.feature.search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.search.presentation.flows.SearchFlowCollector
import nl.rhaydus.softcover.feature.search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState

class SearchScreenViewModel(
    private val getPreviousSearchQueriesUseCase: GetPreviousSearchQueriesUseCase,
    private val getQueriedBooksUseCase: GetQueriedBooksUseCase,
    private val searchForNameUseCase: SearchForNameUseCase,
    val removeSearchQueryUseCase: RemoveSearchQueryUseCase,
    val removeAllSearchQueriesUseCase: RemoveAllSearchQueriesUseCase,
    flows: List<SearchFlowCollector>,
    appDispatchers: AppDispatchers,
) : ToadViewModel<SearchScreenUiState, SearchEvent, SearchDependencies, SearchFlowCollector, SearchLocalVariables>(
    initialState = SearchScreenUiState(),
    initialLocalVariables = SearchLocalVariables(),
    initialFlowCollectors = flows,
) {
    override val dependencies: SearchDependencies = SearchDependencies(
        searchForNameUseCase = searchForNameUseCase,
        getQueriedBooksUseCase = getQueriedBooksUseCase,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
        getPreviousSearchQueriesUseCase = getPreviousSearchQueriesUseCase,
        removeSearchQueryUseCase = removeSearchQueryUseCase,
        removeAllSearchQueriesUseCase = removeAllSearchQueriesUseCase,
    )

    init {
        startFlowCollectors()
    }

    fun runAction(action: SearchAction) = dispatch(action = action)
}