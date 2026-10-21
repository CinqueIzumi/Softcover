package nl.rhaydus.softcover.showcase.live.progress.action

import nl.rhaydus.softcover.showcase.live.progress.event.ProgressLiveEvent
import nl.rhaydus.softcover.showcase.live.progress.screenmodel.ProgressLiveDependencies
import nl.rhaydus.softcover.showcase.live.progress.state.ProgressLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.progress.state.ProgressLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface ProgressLiveAction : UiAction<
        ProgressLiveDependencies,
        ProgressLiveUiState,
        ProgressLiveEvent,
        ProgressLiveLocalVariables,
        >
