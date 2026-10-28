package nl.rhaydus.softcover.showcase.live.traits.action

import nl.rhaydus.softcover.core.component.sheet.withModel
import nl.rhaydus.softcover.showcase.live.traits.event.HowItReadLiveEvent
import nl.rhaydus.softcover.showcase.live.traits.mapper.withBalance
import nl.rhaydus.softcover.showcase.live.traits.screenmodel.HowItReadLiveDependencies
import nl.rhaydus.softcover.showcase.live.traits.state.HowItReadLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.traits.state.HowItReadLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnBalanceChangedAction(val value: Float?) : HowItReadLiveAction {
    override suspend fun execute(
        dependencies: HowItReadLiveDependencies,
        scope: ActionScope<HowItReadLiveUiState, HowItReadLiveEvent, HowItReadLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = it.sheet.withModel { model -> model.withBalance(value = value) }) }
    }
}
