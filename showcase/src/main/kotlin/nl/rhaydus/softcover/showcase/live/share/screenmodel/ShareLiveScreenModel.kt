package nl.rhaydus.softcover.showcase.live.share.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.share.event.ShareLiveEvent
import nl.rhaydus.softcover.showcase.live.share.state.ShareLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.share.state.ShareLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class ShareLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<ShareLiveUiState, ShareLiveEvent, ShareLiveDependencies, ShareLiveLocalVariables>>,
) : ToadScreenModel<
    ShareLiveUiState,
    ShareLiveEvent,
    ShareLiveDependencies,
    Collector<ShareLiveUiState, ShareLiveEvent, ShareLiveDependencies, ShareLiveLocalVariables>,
    ShareLiveLocalVariables,
    >(
    initialState = ShareLiveUiState(),
    initialLocalVariables = ShareLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = ShareLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<ShareLiveDependencies, ShareLiveUiState, ShareLiveEvent, ShareLiveLocalVariables>,
    ) = dispatch(action = action)
}
