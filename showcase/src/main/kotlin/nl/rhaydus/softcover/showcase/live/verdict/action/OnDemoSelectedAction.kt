package nl.rhaydus.softcover.showcase.live.verdict.action

import nl.rhaydus.softcover.core.component.sheet.open
import nl.rhaydus.softcover.showcase.live.verdict.event.VerdictLiveEvent
import nl.rhaydus.softcover.showcase.live.verdict.mapper.verdictDemoModel
import nl.rhaydus.softcover.showcase.live.verdict.model.VerdictDemo
import nl.rhaydus.softcover.showcase.live.verdict.screenmodel.VerdictLiveDependencies
import nl.rhaydus.softcover.showcase.live.verdict.state.VerdictLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.verdict.state.VerdictLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnDemoSelectedAction(val demo: VerdictDemo) : VerdictLiveAction {
    override suspend fun execute(
        dependencies: VerdictLiveDependencies,
        scope: ActionScope<VerdictLiveUiState, VerdictLiveEvent, VerdictLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = it.sheet.open(model = verdictDemoModel(demo = demo))) }
    }
}
