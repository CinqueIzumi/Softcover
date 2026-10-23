package nl.rhaydus.softcover.showcase.live.callout.action

import nl.rhaydus.softcover.showcase.live.callout.event.CalloutLiveEvent
import nl.rhaydus.softcover.showcase.live.callout.screenmodel.CalloutLiveDependencies
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface CalloutLiveAction : UiAction<
        CalloutLiveDependencies,
        CalloutLiveUiState,
        CalloutLiveEvent,
        CalloutLiveLocalVariables,
        >
