package nl.rhaydus.softcover.showcase.live.sidebar.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.sidebar.event.SidebarLiveEvent
import nl.rhaydus.softcover.showcase.live.sidebar.mapper.sidebarLiveState
import nl.rhaydus.softcover.showcase.live.sidebar.state.SidebarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.sidebar.state.SidebarLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class SidebarLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<SidebarLiveUiState, SidebarLiveEvent, SidebarLiveDependencies, SidebarLiveLocalVariables>>,
) : ToadScreenModel<
    SidebarLiveUiState,
    SidebarLiveEvent,
    SidebarLiveDependencies,
    Collector<SidebarLiveUiState, SidebarLiveEvent, SidebarLiveDependencies, SidebarLiveLocalVariables>,
    SidebarLiveLocalVariables,
    >(
    initialState = sidebarLiveState(),
    initialLocalVariables = SidebarLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = SidebarLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<SidebarLiveDependencies, SidebarLiveUiState, SidebarLiveEvent, SidebarLiveLocalVariables>,
    ) = dispatch(action = action)
}
