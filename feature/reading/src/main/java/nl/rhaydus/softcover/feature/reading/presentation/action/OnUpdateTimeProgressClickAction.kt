package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import timber.log.Timber

internal data class OnUpdateTimeProgressClickAction(
    val hours: String,
    val minutes: String,
    val seconds: String,
) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        val bookToUpdate: Book = scope.currentState.bookToUpdate ?: return

        val h = hours.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val m = minutes.toIntOrNull()?.coerceIn(
            0,
            59,
        ) ?: 0
        val s = seconds.toIntOrNull()?.coerceIn(
            0,
            59,
        ) ?: 0

        val total = bookToUpdate.currentEdition?.audioSeconds ?: 0
        val newSeconds = (h * 3600 + m * 60 + s).coerceIn(
            0,
            total,
        )

        scope.currentLocalVariables.bookMutationJobs[bookToUpdate.id]?.cancel()

        val job = dependencies.launch {
            dependencies.recordBookProgressUseCase(
                book = bookToUpdate,
                newSeconds = newSeconds,
            ).onFailure { error ->
                Timber.e("$error")

                scope.setState {
                    it.copy(failedMutationBookIds = it.failedMutationBookIds + bookToUpdate.id)
                }
            }
        }

        scope.setLocalVariables {
            it.copy(bookMutationJobs = it.bookMutationJobs + (bookToUpdate.id to job))
        }

        scope.setState {
            it.copy(
                showProgressSheet = false,
                bookToUpdate = null,
            )
        }
    }
}
