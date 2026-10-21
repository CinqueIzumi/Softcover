package nl.rhaydus.softcover.showcase.live.snackbar.action

import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.showcase.live.snackbar.event.SnackbarLiveEvent
import nl.rhaydus.softcover.showcase.live.snackbar.screenmodel.SnackbarLiveDependencies
import nl.rhaydus.softcover.showcase.live.snackbar.state.SnackbarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.snackbar.state.SnackbarLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnSnackbarSheetPostClickedAction : SnackbarLiveAction {
    override suspend fun execute(
        dependencies: SnackbarLiveDependencies,
        scope: ActionScope<SnackbarLiveUiState, SnackbarLiveEvent, SnackbarLiveLocalVariables>,
    ) {
        SnackBarManager.showSnackbar(title = "Posted from inside the sheet")
    }
}
