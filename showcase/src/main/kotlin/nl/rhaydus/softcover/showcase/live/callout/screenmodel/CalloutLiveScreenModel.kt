package nl.rhaydus.softcover.showcase.live.callout.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.callout.event.CalloutLiveEvent
import nl.rhaydus.softcover.showcase.live.callout.mapper.calloutLiveTopBar
import nl.rhaydus.softcover.showcase.live.callout.mapper.liveBanners
import nl.rhaydus.softcover.showcase.live.callout.mapper.liveCallouts
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class CalloutLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<
        Collector<CalloutLiveUiState, CalloutLiveEvent, CalloutLiveDependencies, CalloutLiveLocalVariables>,
        >,
) : ToadScreenModel<
    CalloutLiveUiState,
    CalloutLiveEvent,
    CalloutLiveDependencies,
    Collector<CalloutLiveUiState, CalloutLiveEvent, CalloutLiveDependencies, CalloutLiveLocalVariables>,
    CalloutLiveLocalVariables,
    >(
    initialState = CalloutLiveUiState(
        topBar = calloutLiveTopBar(),
        banners = liveBanners(),
        callouts = liveCallouts(),
    ),
    initialLocalVariables = CalloutLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = CalloutLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<CalloutLiveDependencies, CalloutLiveUiState, CalloutLiveEvent, CalloutLiveLocalVariables>,
    ) = dispatch(action = action)
}
