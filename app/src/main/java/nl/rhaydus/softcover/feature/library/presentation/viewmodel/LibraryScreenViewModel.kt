package nl.rhaydus.softcover.feature.library.presentation.viewmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadViewModel
import nl.rhaydus.softcover.feature.library.domain.usecase.GetUserBooksAsFlowUseCase
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.flows.LibraryFlowCollector
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

class LibraryScreenViewModel(
    private val getUserBooksAsFlowUseCase: GetUserBooksAsFlowUseCase,
    appDispatchers: AppDispatchers,
    flows: List<LibraryFlowCollector>,
) : ToadViewModel<LibraryUiState, LibraryEvent, LibraryDependencies, LibraryFlowCollector>(
    initialState = LibraryUiState(),
    initialFlowCollectors = flows,
) {
    override val dependencies = LibraryDependencies(
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
        getUserBooksAsFlowUseCase = getUserBooksAsFlowUseCase,
    )

    init {
        startFlowCollectors()
    }

    fun runAction(action: LibraryAction) = dispatch(action = action)
}