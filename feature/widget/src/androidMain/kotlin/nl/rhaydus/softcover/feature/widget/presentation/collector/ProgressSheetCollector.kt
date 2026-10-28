package nl.rhaydus.softcover.feature.widget.presentation.collector

import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressDependencies
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressSheetHost
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.ActionScope

internal class ProgressSheetCollector : UpdateProgressCollector {
    override suspend fun onLaunch(
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
        dependencies: UpdateProgressDependencies,
    ) {
        dependencies.progressSheetSlice.collect(
            host = UpdateProgressSheetHost,
            scope = scope,
        )
    }
}
