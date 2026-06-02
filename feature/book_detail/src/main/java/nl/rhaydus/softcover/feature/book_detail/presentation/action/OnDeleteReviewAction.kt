package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import timber.log.Timber

class OnDeleteReviewAction(
    private val book: Book,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.setState { it.copy(showReviewSheet = false) }

        // Hardcover owns the review, so deleting clears it there (an empty document), offline-replayable;
        // the optimistic local clear is overwritten on the next refresh either way.
        dependencies.updateBookReviewUseCase(
            book = book,
            review = ReviewDocument.EMPTY,
            hasSpoilers = false,
        ).onFailure { error ->
            Timber.e("$error")

            scope.setState { it.copy(failedMutationBookIds = it.failedMutationBookIds + book.id) }
        }
    }
}
