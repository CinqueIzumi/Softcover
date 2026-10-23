package nl.rhaydus.softcover.showcase.live.control.action

import nl.rhaydus.softcover.showcase.live.control.event.ControlLiveEvent
import nl.rhaydus.softcover.showcase.live.control.screenmodel.ControlLiveDependencies
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface ControlLiveAction : UiAction<
        ControlLiveDependencies,
        ControlLiveUiState,
        ControlLiveEvent,
        ControlLiveLocalVariables,
        >
