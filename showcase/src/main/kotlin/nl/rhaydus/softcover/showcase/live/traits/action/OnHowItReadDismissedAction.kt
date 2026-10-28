package nl.rhaydus.softcover.showcase.live.traits.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.showcase.live.traits.event.HowItReadLiveEvent
import nl.rhaydus.softcover.showcase.live.traits.screenmodel.HowItReadLiveDependencies
import nl.rhaydus.softcover.showcase.live.traits.state.HowItReadLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.traits.state.HowItReadLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnHowItReadDismissedAction : HowItReadLiveAction {
    override suspend fun execute(
        dependencies: HowItReadLiveDependencies,
        scope: ActionScope<HowItReadLiveUiState, HowItReadLiveEvent, HowItReadLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = SheetSession.Closed) }
    }
}
