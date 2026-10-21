package nl.rhaydus.softcover.showcase.live.verdict.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.verdict.event.VerdictLiveEvent
import nl.rhaydus.softcover.showcase.live.verdict.state.VerdictLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.verdict.state.VerdictLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class VerdictLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<VerdictLiveUiState, VerdictLiveEvent, VerdictLiveDependencies, VerdictLiveLocalVariables>>,
) : ToadScreenModel<
    VerdictLiveUiState,
    VerdictLiveEvent,
    VerdictLiveDependencies,
    Collector<VerdictLiveUiState, VerdictLiveEvent, VerdictLiveDependencies, VerdictLiveLocalVariables>,
    VerdictLiveLocalVariables,
    >(
    initialState = VerdictLiveUiState(),
    initialLocalVariables = VerdictLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = VerdictLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<VerdictLiveDependencies, VerdictLiveUiState, VerdictLiveEvent, VerdictLiveLocalVariables>,
    ) = dispatch(action = action)
}
