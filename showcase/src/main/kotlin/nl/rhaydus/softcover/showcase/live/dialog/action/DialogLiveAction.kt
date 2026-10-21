package nl.rhaydus.softcover.showcase.live.dialog.action

import nl.rhaydus.softcover.showcase.live.dialog.event.DialogLiveEvent
import nl.rhaydus.softcover.showcase.live.dialog.screenmodel.DialogLiveDependencies
import nl.rhaydus.softcover.showcase.live.dialog.state.DialogLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.dialog.state.DialogLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface DialogLiveAction : UiAction<
        DialogLiveDependencies,
        DialogLiveUiState,
        DialogLiveEvent,
        DialogLiveLocalVariables,
        >
