package nl.rhaydus.softcover.showcase.live.traits.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.traits.event.HowItReadLiveEvent
import nl.rhaydus.softcover.showcase.live.traits.state.HowItReadLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.traits.state.HowItReadLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class HowItReadLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<
        Collector<HowItReadLiveUiState, HowItReadLiveEvent, HowItReadLiveDependencies, HowItReadLiveLocalVariables>,
        >,
) : ToadScreenModel<
    HowItReadLiveUiState,
    HowItReadLiveEvent,
    HowItReadLiveDependencies,
    Collector<HowItReadLiveUiState, HowItReadLiveEvent, HowItReadLiveDependencies, HowItReadLiveLocalVariables>,
    HowItReadLiveLocalVariables,
    >(
    initialState = HowItReadLiveUiState(),
    initialLocalVariables = HowItReadLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = HowItReadLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<HowItReadLiveDependencies, HowItReadLiveUiState, HowItReadLiveEvent, HowItReadLiveLocalVariables>,
    ) = dispatch(action = action)
}
