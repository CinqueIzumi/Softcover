package nl.rhaydus.softcover.showcase.live.progress.action

import nl.rhaydus.softcover.core.component.sheet.withModel
import nl.rhaydus.softcover.showcase.live.progress.event.ProgressLiveEvent
import nl.rhaydus.softcover.showcase.live.progress.mapper.steppingMinutes
import nl.rhaydus.softcover.showcase.live.progress.screenmodel.ProgressLiveDependencies
import nl.rhaydus.softcover.showcase.live.progress.state.ProgressLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.progress.state.ProgressLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnProgressMinutesSteppedAction(val delta: Int) : ProgressLiveAction {
    override suspend fun execute(
        dependencies: ProgressLiveDependencies,
        scope: ActionScope<ProgressLiveUiState, ProgressLiveEvent, ProgressLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = it.sheet.withModel { model -> model.steppingMinutes(delta = delta) }) }
    }
}
