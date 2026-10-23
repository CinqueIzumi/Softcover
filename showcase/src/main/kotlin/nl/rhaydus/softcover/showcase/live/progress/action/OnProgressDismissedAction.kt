package nl.rhaydus.softcover.showcase.live.progress.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.showcase.live.progress.event.ProgressLiveEvent
import nl.rhaydus.softcover.showcase.live.progress.screenmodel.ProgressLiveDependencies
import nl.rhaydus.softcover.showcase.live.progress.state.ProgressLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.progress.state.ProgressLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnProgressDismissedAction : ProgressLiveAction {
    override suspend fun execute(
        dependencies: ProgressLiveDependencies,
        scope: ActionScope<ProgressLiveUiState, ProgressLiveEvent, ProgressLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = SheetSession.Closed) }
    }
}
