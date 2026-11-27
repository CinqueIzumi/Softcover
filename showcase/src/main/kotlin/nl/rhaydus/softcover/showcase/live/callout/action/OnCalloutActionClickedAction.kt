package nl.rhaydus.softcover.showcase.live.callout.action

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.callout.CalloutActionUiModel
import nl.rhaydus.softcover.core.component.callout.CalloutUiModel
import nl.rhaydus.softcover.showcase.live.callout.event.CalloutLiveEvent
import nl.rhaydus.softcover.showcase.live.callout.screenmodel.CalloutLiveDependencies
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnCalloutActionClickedAction(val callout: CalloutUiModel) : CalloutLiveAction {
    override suspend fun execute(
        dependencies: CalloutLiveDependencies,
        scope: ActionScope<CalloutLiveUiState, CalloutLiveEvent, CalloutLiveLocalVariables>,
    ) {
        val detail = callout.callToAction as? CalloutActionUiModel.Detail ?: return

        val next = detail.copy(
            lines = if (detail.lines.isEmpty()) {
                persistentListOf("Too slow, Lost interest", "The middle dragged for me.")
            } else {
                persistentListOf()
            },
        )

        scope.setState { state ->
            state.copy(
                callouts = state.callouts
                    .map { if (it == callout) it.copy(callToAction = next) else it }
                    .toImmutableList(),
            )
        }
    }
}
