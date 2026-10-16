package nl.rhaydus.softcover.feature.widget.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.uibinding.progress.toProgressSheetTab
import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.ActionScope

internal class LastUsedProgressUnitCollector : UpdateProgressCollector {
    override suspend fun onLaunch(
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
        dependencies: UpdateProgressDependencies,
    ) {
        dependencies.getLastUsedProgressUnitAsFlow().collectLatest { unit ->
            scope.setState { it.copy(progressSheetTab = unit.toProgressSheetTab()) }
        }
    }
}
