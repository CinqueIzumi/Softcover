package nl.rhaydus.softcover.showcase.live.selection.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.selection.event.SelectionLiveEvent
import nl.rhaydus.softcover.showcase.live.selection.mapper.selectionLiveState
import nl.rhaydus.softcover.showcase.live.selection.state.SelectionLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.selection.state.SelectionLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class SelectionLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<SelectionLiveUiState, SelectionLiveEvent, SelectionLiveDependencies, SelectionLiveLocalVariables>>,
) : ToadScreenModel<
    SelectionLiveUiState,
    SelectionLiveEvent,
    SelectionLiveDependencies,
    Collector<SelectionLiveUiState, SelectionLiveEvent, SelectionLiveDependencies, SelectionLiveLocalVariables>,
    SelectionLiveLocalVariables,
    >(
    initialState = selectionLiveState(),
    initialLocalVariables = SelectionLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = SelectionLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<SelectionLiveDependencies, SelectionLiveUiState, SelectionLiveEvent, SelectionLiveLocalVariables>,
    ) = dispatch(action = action)
}
