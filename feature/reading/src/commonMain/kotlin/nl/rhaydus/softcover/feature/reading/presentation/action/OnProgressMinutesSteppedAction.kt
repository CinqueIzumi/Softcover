package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft
import nl.rhaydus.softcover.core.uibinding.progress.progressCurrentSeconds
import nl.rhaydus.softcover.core.uibinding.progress.withTimeDraft
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal class OnProgressMinutesSteppedAction(
    private val delta: Int,
) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        val book = scope.currentState.bookToUpdate ?: return

        scope.setState { state ->
            val draft = ProgressTimeDraft
                .resolve(
                    stored = state.progressTimeDraft,
                    currentSeconds = book.progressCurrentSeconds,
                )
                .steppedMinutes(delta = delta)

            state.copy(
                progressTimeDraft = draft,
                progressSheet = state.progressSheet?.withTimeDraft(draft = draft),
            )
        }
    }
}
