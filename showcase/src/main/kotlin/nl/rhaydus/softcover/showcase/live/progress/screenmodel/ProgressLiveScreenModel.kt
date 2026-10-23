package nl.rhaydus.softcover.showcase.live.progress.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.progress.event.ProgressLiveEvent
import nl.rhaydus.softcover.showcase.live.progress.state.ProgressLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.progress.state.ProgressLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class ProgressLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<ProgressLiveUiState, ProgressLiveEvent, ProgressLiveDependencies, ProgressLiveLocalVariables>>,
) : ToadScreenModel<
    ProgressLiveUiState,
    ProgressLiveEvent,
    ProgressLiveDependencies,
    Collector<ProgressLiveUiState, ProgressLiveEvent, ProgressLiveDependencies, ProgressLiveLocalVariables>,
    ProgressLiveLocalVariables,
    >(
    initialState = ProgressLiveUiState(),
    initialLocalVariables = ProgressLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = ProgressLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<ProgressLiveDependencies, ProgressLiveUiState, ProgressLiveEvent, ProgressLiveLocalVariables>,
    ) = dispatch(action = action)
}
