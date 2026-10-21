package nl.rhaydus.softcover.showcase.live.verdict.action

import nl.rhaydus.softcover.core.component.sheet.withModel
import nl.rhaydus.softcover.showcase.live.verdict.event.VerdictLiveEvent
import nl.rhaydus.softcover.showcase.live.verdict.mapper.withSpoilers
import nl.rhaydus.softcover.showcase.live.verdict.screenmodel.VerdictLiveDependencies
import nl.rhaydus.softcover.showcase.live.verdict.state.VerdictLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.verdict.state.VerdictLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnVerdictSpoilersToggledAction(val checked: Boolean) : VerdictLiveAction {
    override suspend fun execute(
        dependencies: VerdictLiveDependencies,
        scope: ActionScope<VerdictLiveUiState, VerdictLiveEvent, VerdictLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = it.sheet.withModel { model -> model.withSpoilers(checked = checked) }) }
    }
}
