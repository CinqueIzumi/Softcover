package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.ActionScope

internal data class OnUpdatePercentageProgressClickAction(val newPercentage: String) : UpdateProgressAction {
    override suspend fun execute(
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        val book = scope.currentState.bookToUpdate ?: return

        val fraction = ((newPercentage.toDoubleOrNull() ?: 0.0) / 100.0).coerceIn(
            0.0,
            1.0,
        )

        val edition = book.currentEdition ?: return

        val newPage = if (edition.isAudiobook) {
            null
        } else {
            (fraction * (edition.pages ?: book.defaultEdition?.pages ?: 0)).toInt()
        }

        val newSeconds = if (edition.isAudiobook) {
            (fraction * (edition.audioSeconds ?: 0)).toInt()
        } else {
            null
        }

        recordProgress(
            dependencies = dependencies,
            scope = scope,
            book = book,
            newPage = newPage,
            newSeconds = newSeconds,
        )
    }
}
