package nl.rhaydus.softcover.showcase.live.control.action

import nl.rhaydus.softcover.core.component.control.ThemeTilePainting
import nl.rhaydus.softcover.showcase.live.control.event.ControlLiveEvent
import nl.rhaydus.softcover.showcase.live.control.mapper.themeTiles
import nl.rhaydus.softcover.showcase.live.control.screenmodel.ControlLiveDependencies
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnThemeTileSelectedAction(val painting: ThemeTilePainting) : ControlLiveAction {
    override suspend fun execute(
        dependencies: ControlLiveDependencies,
        scope: ActionScope<ControlLiveUiState, ControlLiveEvent, ControlLiveLocalVariables>,
    ) {
        scope.setState { it.copy(themeTiles = themeTiles(selected = painting)) }
    }
}
