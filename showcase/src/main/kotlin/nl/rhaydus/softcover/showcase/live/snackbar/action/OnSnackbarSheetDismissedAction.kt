package nl.rhaydus.softcover.showcase.live.snackbar.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.showcase.live.snackbar.event.SnackbarLiveEvent
import nl.rhaydus.softcover.showcase.live.snackbar.screenmodel.SnackbarLiveDependencies
import nl.rhaydus.softcover.showcase.live.snackbar.state.SnackbarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.snackbar.state.SnackbarLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnSnackbarSheetDismissedAction : SnackbarLiveAction {
    override suspend fun execute(
        dependencies: SnackbarLiveDependencies,
        scope: ActionScope<SnackbarLiveUiState, SnackbarLiveEvent, SnackbarLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = SheetSession.Closed) }
    }
}
