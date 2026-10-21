package nl.rhaydus.softcover.showcase.live.snackbar.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.snackbar.event.SnackbarLiveEvent
import nl.rhaydus.softcover.showcase.live.snackbar.mapper.actionSnackbarTrigger
import nl.rhaydus.softcover.showcase.live.snackbar.mapper.plainSnackbarTrigger
import nl.rhaydus.softcover.showcase.live.snackbar.mapper.sheetSnackbarTrigger
import nl.rhaydus.softcover.showcase.live.snackbar.mapper.snackbarLiveTopBar
import nl.rhaydus.softcover.showcase.live.snackbar.state.SnackbarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.snackbar.state.SnackbarLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class SnackbarLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<
        Collector<SnackbarLiveUiState, SnackbarLiveEvent, SnackbarLiveDependencies, SnackbarLiveLocalVariables>,
        >,
) : ToadScreenModel<
    SnackbarLiveUiState,
    SnackbarLiveEvent,
    SnackbarLiveDependencies,
    Collector<SnackbarLiveUiState, SnackbarLiveEvent, SnackbarLiveDependencies, SnackbarLiveLocalVariables>,
    SnackbarLiveLocalVariables,
    >(
    initialState = SnackbarLiveUiState(
        topBar = snackbarLiveTopBar(),
        plainTrigger = plainSnackbarTrigger(),
        actionTrigger = actionSnackbarTrigger(),
        sheetTrigger = sheetSnackbarTrigger(),
    ),
    initialLocalVariables = SnackbarLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = SnackbarLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<SnackbarLiveDependencies, SnackbarLiveUiState, SnackbarLiveEvent, SnackbarLiveLocalVariables>,
    ) = dispatch(action = action)
}
