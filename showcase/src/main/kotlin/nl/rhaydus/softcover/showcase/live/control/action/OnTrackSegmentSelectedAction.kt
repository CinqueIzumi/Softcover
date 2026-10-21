package nl.rhaydus.softcover.showcase.live.control.action

import nl.rhaydus.softcover.showcase.live.control.event.ControlLiveEvent
import nl.rhaydus.softcover.showcase.live.control.screenmodel.ControlLiveDependencies
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnTrackSegmentSelectedAction(val key: String) : ControlLiveAction {
    override suspend fun execute(
        dependencies: ControlLiveDependencies,
        scope: ActionScope<ControlLiveUiState, ControlLiveEvent, ControlLiveLocalVariables>,
    ) {
        scope.setState { it.copy(trackSegments = it.trackSegments.copy(selected = key)) }
    }
}
