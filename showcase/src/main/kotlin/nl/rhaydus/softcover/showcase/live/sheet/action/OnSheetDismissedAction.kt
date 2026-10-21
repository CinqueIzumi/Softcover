package nl.rhaydus.softcover.showcase.live.sheet.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.showcase.live.sheet.event.SheetLiveEvent
import nl.rhaydus.softcover.showcase.live.sheet.screenmodel.SheetLiveDependencies
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnSheetDismissedAction : SheetLiveAction {
    override suspend fun execute(
        dependencies: SheetLiveDependencies,
        scope: ActionScope<SheetLiveUiState, SheetLiveEvent, SheetLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = SheetSession.Closed) }
    }
}
