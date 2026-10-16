package nl.rhaydus.softcover.feature.widget.progress

import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.uibinding.progress.toProgressUnit
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
