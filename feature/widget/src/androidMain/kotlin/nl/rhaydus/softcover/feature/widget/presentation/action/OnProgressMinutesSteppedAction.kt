package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft
import nl.rhaydus.softcover.core.uibinding.progress.progressCurrentSeconds
import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.ActionScope

internal class OnProgressMinutesSteppedAction(private val delta: Int) : UpdateProgressAction {
    override suspend fun execute(
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        val book = scope.currentState.bookToUpdate ?: return

        scope.setState { state ->
            val draft = ProgressTimeDraft
                .resolve(
                    stored = state.progressTimeDraft,
                    currentSeconds = book.progressCurrentSeconds,
                )
                .steppedMinutes(delta = delta)

            state.copy(progressTimeDraft = draft)
        }
    }
}
