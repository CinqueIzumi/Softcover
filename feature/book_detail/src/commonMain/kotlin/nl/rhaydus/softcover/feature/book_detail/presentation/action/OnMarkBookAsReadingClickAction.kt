package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnMarkBookAsReadingClickAction(private val book: Book) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.currentLocalVariables.bookMutationJobs[book.id]?.cancel()

        // A previewed or scanned edition (off-shelf) becomes the created user book's edition.
        val editionId = scope.currentState.previewEdition?.id ?: scope.currentState.scannedEditionId

        val job = dependencies.launch {
            dependencies.markBookAsReadingUseCase(
                book = book,
                editionId = editionId,
            ).onFailure { error ->
                AppLog.e("$error")

                scope.setState { it.copy(failedMutationBookIds = it.failedMutationBookIds + book.id) }
            }
        }

        scope.setLocalVariables {
            it.copy(bookMutationJobs = it.bookMutationJobs + (book.id to job))
        }
    }
}
