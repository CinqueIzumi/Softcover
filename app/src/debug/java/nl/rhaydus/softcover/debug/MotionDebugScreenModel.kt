package nl.rhaydus.softcover.debug

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.toad.ToadScreenModel

class MotionDebugScreenModel(
    appDispatchers: AppDispatchers,
) : ToadScreenModel<
    MotionDebugUiState,
    MotionDebugEvent,
    MotionDebugDependencies,
    MotionDebugCollector,
    MotionDebugLocalVariables,
    >(
    initialState = MotionDebugUiState(),
    initialLocalVariables = MotionDebugLocalVariables(),
    initializers = emptyList(),
) {
    override val dependencies = MotionDebugDependencies(
        mainDispatcher = appDispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }
}
