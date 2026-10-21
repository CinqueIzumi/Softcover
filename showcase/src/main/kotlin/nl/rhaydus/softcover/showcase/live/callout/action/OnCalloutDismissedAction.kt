package nl.rhaydus.softcover.showcase.live.callout.action

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.callout.CalloutUiModel
import nl.rhaydus.softcover.showcase.live.callout.event.CalloutLiveEvent
import nl.rhaydus.softcover.showcase.live.callout.screenmodel.CalloutLiveDependencies
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnCalloutDismissedAction(val callout: CalloutUiModel) : CalloutLiveAction {
    override suspend fun execute(
        dependencies: CalloutLiveDependencies,
        scope: ActionScope<CalloutLiveUiState, CalloutLiveEvent, CalloutLiveLocalVariables>,
    ) {
        scope.setState { state ->
            state.copy(callouts = state.callouts.filterNot { it == callout }.toImmutableList())
        }
    }
}
