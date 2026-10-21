package nl.rhaydus.softcover.showcase.live.celebration.action

import nl.rhaydus.softcover.showcase.live.celebration.event.CelebrationLiveEvent
import nl.rhaydus.softcover.showcase.live.celebration.screenmodel.CelebrationLiveDependencies
import nl.rhaydus.softcover.showcase.live.celebration.state.CelebrationLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.celebration.state.CelebrationLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface CelebrationLiveAction : UiAction<
        CelebrationLiveDependencies,
        CelebrationLiveUiState,
        CelebrationLiveEvent,
        CelebrationLiveLocalVariables,
        >
