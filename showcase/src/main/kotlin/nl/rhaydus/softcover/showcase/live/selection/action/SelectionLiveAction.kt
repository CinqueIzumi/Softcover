package nl.rhaydus.softcover.showcase.live.selection.action

import nl.rhaydus.softcover.showcase.live.selection.event.SelectionLiveEvent
import nl.rhaydus.softcover.showcase.live.selection.screenmodel.SelectionLiveDependencies
import nl.rhaydus.softcover.showcase.live.selection.state.SelectionLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.selection.state.SelectionLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface SelectionLiveAction : UiAction<
        SelectionLiveDependencies,
        SelectionLiveUiState,
        SelectionLiveEvent,
        SelectionLiveLocalVariables,
        >
