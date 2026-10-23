package nl.rhaydus.softcover.showcase.live.topbar.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.topbar.event.TopbarLiveEvent
import nl.rhaydus.softcover.showcase.live.topbar.mapper.topbarLiveState
import nl.rhaydus.softcover.showcase.live.topbar.state.TopbarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.topbar.state.TopbarLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class TopbarLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<TopbarLiveUiState, TopbarLiveEvent, TopbarLiveDependencies, TopbarLiveLocalVariables>>,
) : ToadScreenModel<
    TopbarLiveUiState,
    TopbarLiveEvent,
    TopbarLiveDependencies,
    Collector<TopbarLiveUiState, TopbarLiveEvent, TopbarLiveDependencies, TopbarLiveLocalVariables>,
    TopbarLiveLocalVariables,
    >(
    initialState = topbarLiveState(),
    initialLocalVariables = TopbarLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = TopbarLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<TopbarLiveDependencies, TopbarLiveUiState, TopbarLiveEvent, TopbarLiveLocalVariables>,
    ) = dispatch(action = action)
}
