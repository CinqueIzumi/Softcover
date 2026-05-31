package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import timber.log.Timber

class OnMarkBookAsWantToReadClickAction(private val book: Book) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.currentLocalVariables.bookMutationJobs[book.id]?.cancel()

        // A previewed edition (off-shelf) becomes the created user book's edition.
        val editionId = scope.currentState.previewEdition?.id

        val job = dependencies.launch {
            dependencies.markBookAsWantToReadUseCase(book = book, editionId = editionId).onFailure { error ->
                Timber.e("$error")

                scope.setState { it.copy(failedMutationBookIds = it.failedMutationBookIds + book.id) }
            }
        }

        scope.setLocalVariables {
            it.copy(bookMutationJobs = it.bookMutationJobs + (book.id to job))
        }
    }
}
