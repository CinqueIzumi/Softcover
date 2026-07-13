package nl.rhaydus.softcover.feature.lists.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.feature.lists.domain.usecase.CreateListUseCase
import nl.rhaydus.softcover.feature.lists.presentation.action.CreateListAction
import nl.rhaydus.softcover.feature.lists.presentation.collector.CreateListCollector
import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState
import nl.rhaydus.softcover.feature.lists.presentation.state.LocalCreateListVariables
import nl.rhaydus.toad.ToadScreenModel

internal class CreateListScreenModel(
    private val createListUseCase: CreateListUseCase,
    dispatchers: AppDispatchers,
) : ToadScreenModel<CreateListUiState, CreateListEvent, CreateListDependencies, CreateListCollector, LocalCreateListVariables>(
    initializers = emptyList(),
    initialState = CreateListUiState(),
    initialLocalVariables = LocalCreateListVariables(),
) {
    override val dependencies: CreateListDependencies = CreateListDependencies(
        createListUseCase = createListUseCase,
        coroutineScope = screenModelScope,
        mainDispatcher = dispatchers.main,
    )

    fun runAction(action: CreateListAction) = dispatch(action)
}
