package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.softcover.core.component.progress.ProgressSheetEvent
import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressDependencies
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressSheetHost
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.ActionScope

internal data class OnProgressSheetEventAction(val event: ProgressSheetEvent) : UpdateProgressAction {
    override suspend fun execute(
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        dependencies.progressSheetSlice.onEvent(
            event = event,
            host = UpdateProgressSheetHost,
            dependencies = dependencies,
            scope = scope,
        )
    }
}
