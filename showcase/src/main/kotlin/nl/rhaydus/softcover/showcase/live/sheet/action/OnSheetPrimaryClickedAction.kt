package nl.rhaydus.softcover.showcase.live.sheet.action

import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.showcase.live.sheet.event.SheetLiveEvent
import nl.rhaydus.softcover.showcase.live.sheet.screenmodel.SheetLiveDependencies
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnSheetPrimaryClickedAction : SheetLiveAction {
    override suspend fun execute(
        dependencies: SheetLiveDependencies,
        scope: ActionScope<SheetLiveUiState, SheetLiveEvent, SheetLiveLocalVariables>,
    ) {
        SnackBarManager.showSnackbar(title = "Noted. Nothing was saved.")
    }
}
