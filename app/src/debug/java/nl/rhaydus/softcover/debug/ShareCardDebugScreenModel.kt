package nl.rhaydus.softcover.debug

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.toad.ToadScreenModel

class ShareCardDebugScreenModel(
    appDispatchers: AppDispatchers,
) : ToadScreenModel<
    ShareCardDebugUiState,
    ShareCardDebugEvent,
    ShareCardDebugDependencies,
    ShareCardDebugCollector,
    ShareCardDebugLocalVariables,
    >(
    initialState = ShareCardDebugUiState(),
    initialLocalVariables = ShareCardDebugLocalVariables(),
    initializers = emptyList(),
) {
    override val dependencies = ShareCardDebugDependencies(
        mainDispatcher = appDispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }
}
