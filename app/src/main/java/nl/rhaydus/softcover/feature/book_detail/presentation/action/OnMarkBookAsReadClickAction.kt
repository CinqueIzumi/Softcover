package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookMarkedAsReadEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.books.domain.usecase.ShelfMutationOutcome
import timber.log.Timber

class OnMarkBookAsReadClickAction(
    private val book: Book,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.currentLocalVariables.bookMutationJobs[book.id]?.cancel()

        val job = dependencies.launch {
            dependencies.markBookAsReadUseCase(book = book)
                .onSuccess { outcome ->
                    // Celebrate only on a real transition — re-tapping the active "Read" chip
                    // must not replay the burst or rewrite finished_at.
                    if (outcome == ShelfMutationOutcome.Applied) {
                        scope.sendEvent(BookMarkedAsReadEvent())
                    }
                }
                .onFailure { error ->
                    Timber.e("$error")

                    scope.setState { it.copy(failedMutationBookIds = it.failedMutationBookIds + book.id) }
                }
        }

        scope.setLocalVariables {
            it.copy(bookMutationJobs = it.bookMutationJobs + (book.id to job))
        }
    }
}
