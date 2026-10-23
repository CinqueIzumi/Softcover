package nl.rhaydus.softcover.showcase.live.control.action

import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.showcase.live.control.event.ControlLiveEvent
import nl.rhaydus.softcover.showcase.live.control.mapper.paletteTiles
import nl.rhaydus.softcover.showcase.live.control.screenmodel.ControlLiveDependencies
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnPaletteTileSelectedAction(val palette: SpinePalette) : ControlLiveAction {
    override suspend fun execute(
        dependencies: ControlLiveDependencies,
        scope: ActionScope<ControlLiveUiState, ControlLiveEvent, ControlLiveLocalVariables>,
    ) {
        scope.setState { it.copy(paletteTiles = paletteTiles(selected = palette)) }
    }
}
