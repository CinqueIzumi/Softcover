package nl.rhaydus.softcover.showcase.live.traits.action

import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.showcase.live.traits.event.HowItReadLiveEvent
import nl.rhaydus.softcover.showcase.live.traits.mapper.hasAnswers
import nl.rhaydus.softcover.showcase.live.traits.mapper.howItReadBlock
import nl.rhaydus.softcover.showcase.live.traits.mapper.howItReadEmptyBlock
import nl.rhaydus.softcover.showcase.live.traits.screenmodel.HowItReadLiveDependencies
import nl.rhaydus.softcover.showcase.live.traits.state.HowItReadLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.traits.state.HowItReadLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnHowItReadSavedAction : HowItReadLiveAction {
    override suspend fun execute(
        dependencies: HowItReadLiveDependencies,
        scope: ActionScope<HowItReadLiveUiState, HowItReadLiveEvent, HowItReadLiveLocalVariables>,
    ) {
        scope.setState { state ->
            val answered = state.sheet.modelOrNull?.hasAnswers() == true

            state.copy(
                block = if (answered) howItReadBlock(expanded = false) else howItReadEmptyBlock(),
            )
        }
    }
}
