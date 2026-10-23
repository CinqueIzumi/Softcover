package nl.rhaydus.softcover.showcase.live.verdict.action

import nl.rhaydus.softcover.showcase.live.verdict.event.VerdictLiveEvent
import nl.rhaydus.softcover.showcase.live.verdict.screenmodel.VerdictLiveDependencies
import nl.rhaydus.softcover.showcase.live.verdict.state.VerdictLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.verdict.state.VerdictLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface VerdictLiveAction : UiAction<
        VerdictLiveDependencies,
        VerdictLiveUiState,
        VerdictLiveEvent,
        VerdictLiveLocalVariables,
        >
