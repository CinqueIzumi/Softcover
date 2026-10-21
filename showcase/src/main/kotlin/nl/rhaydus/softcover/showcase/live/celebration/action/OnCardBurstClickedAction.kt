package nl.rhaydus.softcover.showcase.live.celebration.action

import nl.rhaydus.softcover.showcase.live.celebration.event.CelebrationLiveEvent
import nl.rhaydus.softcover.showcase.live.celebration.screenmodel.CelebrationLiveDependencies
import nl.rhaydus.softcover.showcase.live.celebration.state.CelebrationLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.celebration.state.CelebrationLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnCardBurstClickedAction : CelebrationLiveAction {
    override suspend fun execute(
        dependencies: CelebrationLiveDependencies,
        scope: ActionScope<CelebrationLiveUiState, CelebrationLiveEvent, CelebrationLiveLocalVariables>,
    ) {
        scope.setState { it.copy(cardBurst = it.cardBurst.next()) }
    }
}
