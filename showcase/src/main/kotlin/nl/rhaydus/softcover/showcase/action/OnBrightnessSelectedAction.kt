package nl.rhaydus.softcover.showcase.action

import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.showcase.event.ShowcaseEvent
import nl.rhaydus.softcover.showcase.mapper.showcaseBrightnessChips
import nl.rhaydus.softcover.showcase.screenmodel.ShowcaseDependencies
import nl.rhaydus.softcover.showcase.state.ShowcaseLocalVariables
import nl.rhaydus.softcover.showcase.state.ShowcaseUiState
import nl.rhaydus.toad.ActionScope

internal class OnBrightnessSelectedAction(val brightness: SchemeBrightness) : ShowcaseAction {
    override suspend fun execute(
        dependencies: ShowcaseDependencies,
        scope: ActionScope<ShowcaseUiState, ShowcaseEvent, ShowcaseLocalVariables>,
    ) {
        scope.setState {
            it.copy(
                selectedBrightness = brightness,
                brightnessChips = showcaseBrightnessChips(selected = brightness),
            )
        }
    }
}
