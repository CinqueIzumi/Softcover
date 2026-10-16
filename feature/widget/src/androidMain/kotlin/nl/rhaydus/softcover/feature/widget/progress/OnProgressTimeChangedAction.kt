package nl.rhaydus.softcover.feature.widget.progress

import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft
import nl.rhaydus.softcover.core.uibinding.progress.progressCurrentSeconds
import nl.rhaydus.toad.ActionScope

internal class OnProgressTimeChangedAction(
    private val unit: ProgressTimeUnit,
    private val text: String,
) : UpdateProgressAction {
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
                .withText(
                    unit = unit,
                    text = text,
                )

            state.copy(progressTimeDraft = draft)
        }
    }
}
