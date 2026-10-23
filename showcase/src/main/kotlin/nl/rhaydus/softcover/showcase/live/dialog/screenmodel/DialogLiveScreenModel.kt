package nl.rhaydus.softcover.showcase.live.dialog.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.dialog.event.DialogLiveEvent
import nl.rhaydus.softcover.showcase.live.dialog.mapper.datePickerTrigger
import nl.rhaydus.softcover.showcase.live.dialog.mapper.dialogLiveTopBar
import nl.rhaydus.softcover.showcase.live.dialog.state.DialogLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.dialog.state.DialogLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class DialogLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<DialogLiveUiState, DialogLiveEvent, DialogLiveDependencies, DialogLiveLocalVariables>>,
) : ToadScreenModel<
    DialogLiveUiState,
    DialogLiveEvent,
    DialogLiveDependencies,
    Collector<DialogLiveUiState, DialogLiveEvent, DialogLiveDependencies, DialogLiveLocalVariables>,
    DialogLiveLocalVariables,
    >(
    initialState = DialogLiveUiState(
        topBar = dialogLiveTopBar(),
        trigger = datePickerTrigger(pickedDate = null),
    ),
    initialLocalVariables = DialogLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = DialogLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<DialogLiveDependencies, DialogLiveUiState, DialogLiveEvent, DialogLiveLocalVariables>,
    ) = dispatch(action = action)
}
