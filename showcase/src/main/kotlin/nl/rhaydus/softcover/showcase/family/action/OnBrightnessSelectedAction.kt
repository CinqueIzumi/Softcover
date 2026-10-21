package nl.rhaydus.softcover.showcase.family.action

import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.showcase.family.event.FamilyEvent
import nl.rhaydus.softcover.showcase.family.screenmodel.FamilyDependencies
import nl.rhaydus.softcover.showcase.family.state.FamilyLocalVariables
import nl.rhaydus.softcover.showcase.family.state.FamilyUiState
import nl.rhaydus.toad.ActionScope

internal class OnBrightnessSelectedAction(val brightness: SchemeBrightness) : FamilyAction {
    override suspend fun execute(
        dependencies: FamilyDependencies,
        scope: ActionScope<FamilyUiState, FamilyEvent, FamilyLocalVariables>,
    ) {
        dependencies.themeStore.selectBrightness(brightness)
    }
}
