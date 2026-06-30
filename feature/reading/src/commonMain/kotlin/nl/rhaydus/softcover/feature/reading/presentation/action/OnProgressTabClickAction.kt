package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.model.ProgressSheetTab
import nl.rhaydus.softcover.core.designsystem.presentation.model.toProgressUnit
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal data class OnProgressTabClickAction(val newTab: ProgressSheetTab) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        scope.setState { it.copy(progressSheetTab = newTab) }

        // Remember the unit so the sheet pre-selects it next time it opens.
        dependencies.launch {
            dependencies.setLastUsedProgressUnitUseCase(unit = newTab.toProgressUnit())
        }
    }
}
