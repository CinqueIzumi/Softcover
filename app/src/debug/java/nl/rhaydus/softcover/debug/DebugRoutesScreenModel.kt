package nl.rhaydus.softcover.debug

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.toad.ToadScreenModel

class DebugRoutesScreenModel(
    appDispatchers: AppDispatchers,
) : ToadScreenModel<
    DebugRoutesUiState,
    DebugRoutesEvent,
    DebugRoutesDependencies,
    DebugRoutesCollector,
    DebugRoutesLocalVariables,
    >(
    initialState = DebugRoutesUiState(),
    initialLocalVariables = DebugRoutesLocalVariables(),
    initializers = emptyList(),
) {
    override val dependencies = DebugRoutesDependencies(
        mainDispatcher = appDispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }
}
