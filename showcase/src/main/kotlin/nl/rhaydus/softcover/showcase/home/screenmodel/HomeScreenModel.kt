package nl.rhaydus.softcover.showcase.home.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.home.action.HomeAction
import nl.rhaydus.softcover.showcase.home.event.HomeEvent
import nl.rhaydus.softcover.showcase.home.state.HomeLocalVariables
import nl.rhaydus.softcover.showcase.home.state.HomeUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel

internal class HomeScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<HomeUiState, HomeEvent, HomeDependencies, HomeLocalVariables>>,
) : ToadScreenModel<
    HomeUiState,
    HomeEvent,
    HomeDependencies,
    Collector<HomeUiState, HomeEvent, HomeDependencies, HomeLocalVariables>,
    HomeLocalVariables,
    >(
    initialState = HomeUiState(),
    initialLocalVariables = HomeLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = HomeDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(action: HomeAction) = dispatch(action = action)
}
