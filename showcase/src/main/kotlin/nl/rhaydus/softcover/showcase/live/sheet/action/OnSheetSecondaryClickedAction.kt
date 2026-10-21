package nl.rhaydus.softcover.showcase.live.sheet.action

import nl.rhaydus.softcover.core.component.sheet.withModel
import nl.rhaydus.softcover.showcase.live.sheet.event.SheetLiveEvent
import nl.rhaydus.softcover.showcase.live.sheet.mapper.cleared
import nl.rhaydus.softcover.showcase.live.sheet.screenmodel.SheetLiveDependencies
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnSheetSecondaryClickedAction : SheetLiveAction {
    override suspend fun execute(
        dependencies: SheetLiveDependencies,
        scope: ActionScope<SheetLiveUiState, SheetLiveEvent, SheetLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = it.sheet.withModel { model -> model.cleared() }) }
    }
}
