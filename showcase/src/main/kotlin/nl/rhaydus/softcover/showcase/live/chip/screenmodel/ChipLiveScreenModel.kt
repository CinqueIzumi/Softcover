package nl.rhaydus.softcover.showcase.live.chip.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.chip.event.ChipLiveEvent
import nl.rhaydus.softcover.showcase.live.chip.mapper.chipLiveState
import nl.rhaydus.softcover.showcase.live.chip.state.ChipLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.chip.state.ChipLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class ChipLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<ChipLiveUiState, ChipLiveEvent, ChipLiveDependencies, ChipLiveLocalVariables>>,
) : ToadScreenModel<
    ChipLiveUiState,
    ChipLiveEvent,
    ChipLiveDependencies,
    Collector<ChipLiveUiState, ChipLiveEvent, ChipLiveDependencies, ChipLiveLocalVariables>,
    ChipLiveLocalVariables,
    >(
    initialState = chipLiveState(),
    initialLocalVariables = ChipLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = ChipLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<ChipLiveDependencies, ChipLiveUiState, ChipLiveEvent, ChipLiveLocalVariables>,
    ) = dispatch(action = action)
}
