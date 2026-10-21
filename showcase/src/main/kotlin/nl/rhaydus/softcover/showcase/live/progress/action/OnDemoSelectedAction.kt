package nl.rhaydus.softcover.showcase.live.progress.action

import nl.rhaydus.softcover.core.component.sheet.open
import nl.rhaydus.softcover.showcase.live.progress.event.ProgressLiveEvent
import nl.rhaydus.softcover.showcase.live.progress.mapper.progressDemoModel
import nl.rhaydus.softcover.showcase.live.progress.model.ProgressDemo
import nl.rhaydus.softcover.showcase.live.progress.screenmodel.ProgressLiveDependencies
import nl.rhaydus.softcover.showcase.live.progress.state.ProgressLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.progress.state.ProgressLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnDemoSelectedAction(val demo: ProgressDemo) : ProgressLiveAction {
    override suspend fun execute(
        dependencies: ProgressLiveDependencies,
        scope: ActionScope<ProgressLiveUiState, ProgressLiveEvent, ProgressLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = it.sheet.open(model = progressDemoModel(demo = demo))) }
    }
}
