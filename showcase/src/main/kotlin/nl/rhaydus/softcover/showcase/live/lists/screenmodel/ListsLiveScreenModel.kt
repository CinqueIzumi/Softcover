package nl.rhaydus.softcover.showcase.live.lists.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.lists.event.ListsLiveEvent
import nl.rhaydus.softcover.showcase.live.lists.state.ListsLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.lists.state.ListsLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class ListsLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<ListsLiveUiState, ListsLiveEvent, ListsLiveDependencies, ListsLiveLocalVariables>>,
) : ToadScreenModel<
    ListsLiveUiState,
    ListsLiveEvent,
    ListsLiveDependencies,
    Collector<ListsLiveUiState, ListsLiveEvent, ListsLiveDependencies, ListsLiveLocalVariables>,
    ListsLiveLocalVariables,
    >(
    initialState = ListsLiveUiState(),
    initialLocalVariables = ListsLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = ListsLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<ListsLiveDependencies, ListsLiveUiState, ListsLiveEvent, ListsLiveLocalVariables>,
    ) = dispatch(action = action)
}
