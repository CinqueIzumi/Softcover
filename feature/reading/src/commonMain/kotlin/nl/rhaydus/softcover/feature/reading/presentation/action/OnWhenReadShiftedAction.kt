package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.softcover.core.uibinding.progress.shiftedBy
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal class OnWhenReadShiftedAction(
    private val minutes: Int,
) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
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
