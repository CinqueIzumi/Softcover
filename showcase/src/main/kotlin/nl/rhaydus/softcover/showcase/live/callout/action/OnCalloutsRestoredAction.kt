package nl.rhaydus.softcover.showcase.live.callout.action

import nl.rhaydus.softcover.showcase.live.callout.event.CalloutLiveEvent
import nl.rhaydus.softcover.showcase.live.callout.mapper.liveBanners
import nl.rhaydus.softcover.showcase.live.callout.mapper.liveCallouts
import nl.rhaydus.softcover.showcase.live.callout.screenmodel.CalloutLiveDependencies
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnCalloutsRestoredAction : CalloutLiveAction {
    override suspend fun execute(
        dependencies: CalloutLiveDependencies,
        scope: ActionScope<CalloutLiveUiState, CalloutLiveEvent, CalloutLiveLocalVariables>,
    ) {
        scope.setState {
            it.copy(
                banners = liveBanners(),
                callouts = liveCallouts(),
            )
        }
    }
}
