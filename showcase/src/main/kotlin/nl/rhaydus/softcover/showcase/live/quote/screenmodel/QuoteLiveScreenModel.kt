package nl.rhaydus.softcover.showcase.live.quote.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.quote.event.QuoteLiveEvent
import nl.rhaydus.softcover.showcase.live.quote.state.QuoteLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.quote.state.QuoteLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class QuoteLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<QuoteLiveUiState, QuoteLiveEvent, QuoteLiveDependencies, QuoteLiveLocalVariables>>,
) : ToadScreenModel<
    QuoteLiveUiState,
    QuoteLiveEvent,
    QuoteLiveDependencies,
    Collector<QuoteLiveUiState, QuoteLiveEvent, QuoteLiveDependencies, QuoteLiveLocalVariables>,
    QuoteLiveLocalVariables,
    >(
    initialState = QuoteLiveUiState(),
    initialLocalVariables = QuoteLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = QuoteLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<QuoteLiveDependencies, QuoteLiveUiState, QuoteLiveEvent, QuoteLiveLocalVariables>,
    ) = dispatch(action = action)
}
