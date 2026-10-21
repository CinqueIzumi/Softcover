package nl.rhaydus.softcover.showcase.live.chrome.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.chrome.event.ChromeLiveEvent
import nl.rhaydus.softcover.showcase.live.chrome.mapper.chromeLiveState
import nl.rhaydus.softcover.showcase.live.chrome.state.ChromeLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.chrome.state.ChromeLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class ChromeLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<ChromeLiveUiState, ChromeLiveEvent, ChromeLiveDependencies, ChromeLiveLocalVariables>>,
) : ToadScreenModel<
    ChromeLiveUiState,
    ChromeLiveEvent,
    ChromeLiveDependencies,
    Collector<ChromeLiveUiState, ChromeLiveEvent, ChromeLiveDependencies, ChromeLiveLocalVariables>,
    ChromeLiveLocalVariables,
    >(
    initialState = chromeLiveState(),
    initialLocalVariables = ChromeLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = ChromeLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<ChromeLiveDependencies, ChromeLiveUiState, ChromeLiveEvent, ChromeLiveLocalVariables>,
    ) = dispatch(action = action)
}
