package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal data class OnMarkBookAsReadClickAction(
    val book: Book,
    val actionAt: String? = null,
) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        scope.currentLocalVariables.bookMutationJobs[book.id]?.cancel()

        val job = dependencies.launch {
            dependencies.markBookAsReadUseCase(
                book = book,
                actionAt = actionAt,
            )
                .onSuccess { outcome ->
                    // Only a genuine transition prompts for a verdict - re-tapping an already-Read
                    // book (a no-op) must not reopen the sheet.
                    if (outcome == ShelfMutationOutcome.Applied) {
                        scope.setState { it.copy(verdictPromptBook = book) }
                    }
                }
                .onFailure { error ->
                    AppLog.e("$error")

                    scope.setState { it.copy(failedMutationBookIds = it.failedMutationBookIds + book.id) }
                }
        }

        scope.setLocalVariables {
            it.copy(bookMutationJobs = it.bookMutationJobs + (book.id to job))
        }
    }
}
