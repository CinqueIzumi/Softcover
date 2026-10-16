package nl.rhaydus.softcover.feature.widget.progress

import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.softcover.core.uibinding.progress.shiftedBy
import nl.rhaydus.toad.ActionScope

internal class OnWhenReadShiftedAction(private val minutes: Int) : UpdateProgressAction {
    override suspend fun execute(
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        val now = currentLocalDateTime()

        scope.setState {
            it.copy(
                readAt = it.readAt.shiftedBy(
                    minutes = minutes,
                    now = now,
                ),
            )
        }
    }
}
