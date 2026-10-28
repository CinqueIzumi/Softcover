package nl.rhaydus.softcover.showcase.live.traits.action

import nl.rhaydus.softcover.core.component.traits.HowItReadBlockUiModel
import nl.rhaydus.softcover.showcase.live.traits.event.HowItReadLiveEvent
import nl.rhaydus.softcover.showcase.live.traits.mapper.howItReadBlock
import nl.rhaydus.softcover.showcase.live.traits.screenmodel.HowItReadLiveDependencies
import nl.rhaydus.softcover.showcase.live.traits.state.HowItReadLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.traits.state.HowItReadLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnBlockToggledAction : HowItReadLiveAction {
    override suspend fun execute(
        dependencies: HowItReadLiveDependencies,
        scope: ActionScope<HowItReadLiveUiState, HowItReadLiveEvent, HowItReadLiveLocalVariables>,
    ) {
        scope.setState { state ->
            val expanded = (state.block as? HowItReadBlockUiModel.Filled)?.expanded == true

            state.copy(block = howItReadBlock(expanded = expanded.not()))
        }
    }
}
