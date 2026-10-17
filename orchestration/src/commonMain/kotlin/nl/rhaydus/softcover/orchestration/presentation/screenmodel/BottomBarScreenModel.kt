package nl.rhaydus.softcover.orchestration.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.orchestration.presentation.collector.BottomBarCollector
import nl.rhaydus.softcover.orchestration.presentation.event.BottomBarEvent
import nl.rhaydus.softcover.orchestration.presentation.state.BottomBarLocalVariables
import nl.rhaydus.softcover.orchestration.presentation.state.BottomBarUiState
import nl.rhaydus.toad.ToadScreenModel

internal class BottomBarScreenModel(
    appDispatchers: AppDispatchers,
) : ToadScreenModel<BottomBarUiState, BottomBarEvent, BottomBarDependencies, BottomBarCollector, BottomBarLocalVariables>(
    initialState = BottomBarUiState(),
    initialLocalVariables = BottomBarLocalVariables,
    initializers = emptyList(),
) {
    override val dependencies = BottomBarDependencies(
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )
}
