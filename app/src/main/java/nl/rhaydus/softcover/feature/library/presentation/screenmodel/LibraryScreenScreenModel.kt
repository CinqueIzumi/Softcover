package nl.rhaydus.softcover.feature.library.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadScreenModel
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.RefreshUserBooksUseCase
import nl.rhaydus.softcover.feature.deadlines.domain.usecase.ObserveAllBookDeadlinesUseCase
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.flows.LibraryInitializer
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetLibraryGridLayoutAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibraryGridLayoutUseCase

class LibraryScreenScreenModel(
    private val getWantToReadUserBooksUseCase: GetWantToReadUserBooksUseCase,
    private val getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    private val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    private val getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase,
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    private val refreshUserBooksUseCase: RefreshUserBooksUseCase,
    private val getAllUserListsUseCase: GetAllUserListsUseCase,
    private val getLibraryGridLayoutAsFlowUseCase: GetLibraryGridLayoutAsFlowUseCase,
    private val setLibraryGridLayoutUseCase: SetLibraryGridLayoutUseCase,
    private val observeAllBookDeadlinesUseCase: ObserveAllBookDeadlinesUseCase,
    private val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    appDispatchers: AppDispatchers,
    flows: List<LibraryInitializer>,
) : ToadScreenModel<LibraryUiState, LibraryEvent, LibraryDependencies, LibraryInitializer, LibraryLocalVariables>(
    initialState = LibraryUiState(),
    initialLocalVariables = LibraryLocalVariables(),
    initializers = flows,
) {
    override val dependencies = LibraryDependencies(
        getAllUserBooksUseCase = getAllUserBooksUseCase,
        getWantToReadUserBooksUseCase = getWantToReadUserBooksUseCase,
        getCurrentlyReadingUserBooksUseCase = getCurrentlyReadingUserBooksUseCase,
        getReadUserBooksUseCase = getReadUserBooksUseCase,
        getDidNotFinishUserBooksUseCase = getDidNotFinishUserBooksUseCase,
        mainDispatcher = appDispatchers.main,
        refreshUserBooksUseCase = refreshUserBooksUseCase,
        getAllUserListsUseCase = getAllUserListsUseCase,
        getLibraryGridLayoutAsFlowUseCase = getLibraryGridLayoutAsFlowUseCase,
        setLibraryGridLayoutUseCase = setLibraryGridLayoutUseCase,
        observeAllBookDeadlinesUseCase = observeAllBookDeadlinesUseCase,
        getDateStyleAsFlowUseCase = getDateStyleAsFlowUseCase,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: LibraryAction) = dispatch(action = action)
}