package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal data class OnUpdatePageProgressClickAction(val newPage: String) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        val bookToUpdate: Book = scope.currentState.bookToUpdate ?: return

        val newPageValue = newPage.toIntOrNull() ?: 0

        scope.currentLocalVariables.bookMutationJobs[bookToUpdate.id]?.cancel()

        val job = dependencies.launch {
            dependencies.recordBookProgressUseCase(
                book = bookToUpdate,
                newPage = newPageValue,
                newSeconds = null,
            )
                .onSuccess { outcome ->
                    // Only a genuine finish transition raises the verdict prompt — re-recording the
                    // last page on an already-read book returns NoChange and must stay silent.
                    if (outcome == ShelfMutationOutcome.Applied) {
                        scope.setState { it.copy(verdictPromptBook = bookToUpdate) }
                    }
                }
                .onFailure { error ->
                    AppLog.e("$error")

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
