package nl.rhaydus.softcover.showcase.live.sheet.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.sheet.event.SheetLiveEvent
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class SheetLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<SheetLiveUiState, SheetLiveEvent, SheetLiveDependencies, SheetLiveLocalVariables>>,
) : ToadScreenModel<
    SheetLiveUiState,
    SheetLiveEvent,
    SheetLiveDependencies,
    Collector<SheetLiveUiState, SheetLiveEvent, SheetLiveDependencies, SheetLiveLocalVariables>,
    SheetLiveLocalVariables,
    >(
    initialState = SheetLiveUiState(),
    initialLocalVariables = SheetLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = SheetLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<SheetLiveDependencies, SheetLiveUiState, SheetLiveEvent, SheetLiveLocalVariables>,
    ) = dispatch(action = action)
}
