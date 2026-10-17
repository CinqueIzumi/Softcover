package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.uibinding.progress.toProgressUnit
import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.ActionScope

internal data class OnProgressTabClickAction(val newTab: ProgressSheetTab) : UpdateProgressAction {
    override suspend fun execute(
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        scope.setState { it.copy(progressSheetTab = newTab) }

        dependencies.launch {
            dependencies.setLastUsedProgressUnit(unit = newTab.toProgressUnit())
        }
    }
}
