package nl.rhaydus.softcover.showcase.family.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.showcase.family.event.FamilyEvent
import nl.rhaydus.softcover.showcase.family.screenmodel.FamilyDependencies
import nl.rhaydus.softcover.showcase.family.state.FamilyLocalVariables
import nl.rhaydus.softcover.showcase.family.state.FamilyUiState
import nl.rhaydus.toad.ActionScope

internal class OnAppearanceDismissedAction : FamilyAction {
    override suspend fun execute(
        dependencies: FamilyDependencies,
        scope: ActionScope<FamilyUiState, FamilyEvent, FamilyLocalVariables>,
    ) {
        scope.setState { it.copy(appearance = it.appearance.copy(sheet = SheetSession.Closed)) }
    }
}
