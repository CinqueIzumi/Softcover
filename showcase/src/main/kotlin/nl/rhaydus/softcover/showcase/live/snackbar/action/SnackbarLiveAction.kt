package nl.rhaydus.softcover.showcase.live.snackbar.action

import nl.rhaydus.softcover.showcase.live.snackbar.event.SnackbarLiveEvent
import nl.rhaydus.softcover.showcase.live.snackbar.screenmodel.SnackbarLiveDependencies
import nl.rhaydus.softcover.showcase.live.snackbar.state.SnackbarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.snackbar.state.SnackbarLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface SnackbarLiveAction : UiAction<
        SnackbarLiveDependencies,
        SnackbarLiveUiState,
        SnackbarLiveEvent,
        SnackbarLiveLocalVariables,
        >
