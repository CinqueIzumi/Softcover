package nl.rhaydus.softcover.showcase.live.callout.action

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.callout.BannerVariant
import nl.rhaydus.softcover.showcase.live.callout.event.CalloutLiveEvent
import nl.rhaydus.softcover.showcase.live.callout.screenmodel.CalloutLiveDependencies
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnBannerDismissedAction(val variant: BannerVariant) : CalloutLiveAction {
    override suspend fun execute(
        dependencies: CalloutLiveDependencies,
        scope: ActionScope<CalloutLiveUiState, CalloutLiveEvent, CalloutLiveLocalVariables>,
    ) {
        scope.setState { state ->
            state.copy(
                banners = state.banners
                    .map { if (it.variant == variant) it.copy(visible = false) else it }
                    .toImmutableList(),
            )
        }
    }
}
