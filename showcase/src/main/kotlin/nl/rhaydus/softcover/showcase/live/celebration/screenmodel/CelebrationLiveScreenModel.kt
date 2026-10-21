package nl.rhaydus.softcover.showcase.live.celebration.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstScale
import nl.rhaydus.softcover.showcase.live.celebration.event.CelebrationLiveEvent
import nl.rhaydus.softcover.showcase.live.celebration.mapper.celebrationLiveTopBar
import nl.rhaydus.softcover.showcase.live.celebration.mapper.restingBurst
import nl.rhaydus.softcover.showcase.live.celebration.state.CelebrationLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.celebration.state.CelebrationLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class CelebrationLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<
        Collector<
            CelebrationLiveUiState,
            CelebrationLiveEvent,
            CelebrationLiveDependencies,
            CelebrationLiveLocalVariables,
            >,
        >,
) : ToadScreenModel<
    CelebrationLiveUiState,
    CelebrationLiveEvent,
    CelebrationLiveDependencies,
    Collector<
        CelebrationLiveUiState,
        CelebrationLiveEvent,
        CelebrationLiveDependencies,
        CelebrationLiveLocalVariables,
        >,
    CelebrationLiveLocalVariables,
    >(
    initialState = CelebrationLiveUiState(
        topBar = celebrationLiveTopBar(),
        cardBurst = restingBurst(scale = MarkAsReadBurstScale.Card),
        screenBurst = restingBurst(scale = MarkAsReadBurstScale.Screen),
    ),
    initialLocalVariables = CelebrationLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = CelebrationLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<CelebrationLiveDependencies, CelebrationLiveUiState, CelebrationLiveEvent, CelebrationLiveLocalVariables>,
    ) = dispatch(action = action)
}
