package nl.rhaydus.softcover.feature.search.presentation.viewmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadViewModel
import nl.rhaydus.softcover.feature.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.GetPreviousSearchQueriesUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.GetQueriedBooksUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.RemoveAllSearchQueriesUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.RemoveSearchQueryUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.SearchForNameUseCase
import nl.rhaydus.softcover.feature.search.presentation.action.SearchAction
import nl.rhaydus.softcover.feature.search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.search.presentation.flows.SearchInitializer
import nl.rhaydus.softcover.feature.search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState

class SearchScreenViewModel(
    private val getPreviousSearchQueriesUseCase: GetPreviousSearchQueriesUseCase,
    private val getQueriedBooksUseCase: GetQueriedBooksUseCase,
    private val searchForNameUseCase: SearchForNameUseCase,
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    private val removeSearchQueryUseCase: RemoveSearchQueryUseCase,
    private val removeAllSearchQueriesUseCase: RemoveAllSearchQueriesUseCase,
    private val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    flows: List<SearchInitializer>,
    appDispatchers: AppDispatchers,
) : ToadViewModel<SearchScreenUiState, SearchEvent, SearchDependencies, SearchInitializer, SearchLocalVariables>(
    initialState = SearchScreenUiState(),
    initialLocalVariables = SearchLocalVariables(),
    initializers = flows,
) {
    override val dependencies: SearchDependencies = SearchDependencies(
        searchForNameUseCase = searchForNameUseCase,
        getQueriedBooksUseCase = getQueriedBooksUseCase,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
        getPreviousSearchQueriesUseCase = getPreviousSearchQueriesUseCase,
        removeSearchQueryUseCase = removeSearchQueryUseCase,
        getAllUserBooksUseCase = getAllUserBooksUseCase,
        removeAllSearchQueriesUseCase = removeAllSearchQueriesUseCase,
        markBookAsWantToReadUseCase = markBookAsWantToReadUseCase,
    )

    init {
        startInitializers()
    }

    fun runAction(action: SearchAction) = dispatch(action = action)
}