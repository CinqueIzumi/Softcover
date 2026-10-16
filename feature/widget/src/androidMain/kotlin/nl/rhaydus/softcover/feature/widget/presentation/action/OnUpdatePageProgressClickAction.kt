package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.ActionScope

internal data class OnUpdatePageProgressClickAction(val newPage: String) : UpdateProgressAction {
    override suspend fun execute(
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        val book = scope.currentState.bookToUpdate ?: return

        recordProgress(
            dependencies = dependencies,
            scope = scope,
            book = book,
            newPage = newPage.toIntOrNull() ?: 0,
            newSeconds = null,
        )
    }
}
