package nl.rhaydus.softcover.showcase.live.chrome.action

import nl.rhaydus.softcover.showcase.live.chrome.event.ChromeLiveEvent
import nl.rhaydus.softcover.showcase.live.chrome.screenmodel.ChromeLiveDependencies
import nl.rhaydus.softcover.showcase.live.chrome.state.ChromeLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.chrome.state.ChromeLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface ChromeLiveAction : UiAction<
        ChromeLiveDependencies,
        ChromeLiveUiState,
        ChromeLiveEvent,
        ChromeLiveLocalVariables,
        >
