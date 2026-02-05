package nl.rhaydus.softcover.feature.library.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadScreenModel
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.InitializeUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.RefreshUserBooksUseCase
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.flows.LibraryInitializer
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

class LibraryScreenScreenModel(
    private val getWantToReadUserBooksUseCase: GetWantToReadUserBooksUseCase,
    private val getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    private val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    private val getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase,
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    private val refreshUserBooksUseCase: RefreshUserBooksUseCase,
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
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: LibraryAction) = dispatch(action = action)
}