package nl.rhaydus.softcover.showcase.live.control.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.control.event.ControlLiveEvent
import nl.rhaydus.softcover.showcase.live.control.mapper.controlLiveState
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class ControlLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<ControlLiveUiState, ControlLiveEvent, ControlLiveDependencies, ControlLiveLocalVariables>>,
) : ToadScreenModel<
    ControlLiveUiState,
    ControlLiveEvent,
    ControlLiveDependencies,
    Collector<ControlLiveUiState, ControlLiveEvent, ControlLiveDependencies, ControlLiveLocalVariables>,
    ControlLiveLocalVariables,
    >(
    initialState = controlLiveState(),
    initialLocalVariables = ControlLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = ControlLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<ControlLiveDependencies, ControlLiveUiState, ControlLiveEvent, ControlLiveLocalVariables>,
    ) = dispatch(action = action)
}
