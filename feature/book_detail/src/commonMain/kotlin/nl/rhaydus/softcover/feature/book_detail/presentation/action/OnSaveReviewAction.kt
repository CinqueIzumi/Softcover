package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.canonical
import nl.rhaydus.softcover.core.domain.model.isBlank
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnSaveReviewAction(
    private val book: Book,
    private val review: ReviewDocument,
    private val hasSpoilers: Boolean,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.setState { it.copy(showReviewSheet = false) }

        // Skip the round-trip when nothing meaningful changed. Both the review content and the
        // whole-review spoiler flag are user-editable, so a spoiler-only toggle must still save. The
        // documents are compared in canonical form so identical content with different run slicing
        // (the server and the editor slice runs differently) doesn't read as a change.
        val newReview: ReviewDocument = review.canonical()
        val existingReview: ReviewDocument = (book.userBook?.reviewDocument ?: ReviewDocument.EMPTY).canonical()
        val existingHasSpoilers: Boolean = book.userBook?.reviewHasSpoilers ?: false

        val sameReview: Boolean = if (newReview.isBlank() && existingReview.isBlank()) {
            true
        } else {
            newReview == existingReview
        }

        if (sameReview && hasSpoilers == existingHasSpoilers) return

        dependencies.updateBookReviewUseCase(
            book = book,
            review = newReview,
            hasSpoilers = hasSpoilers,
        ).onFailure { error ->
            AppLog.e("$error")

            scope.setState { it.copy(failedMutationBookIds = it.failedMutationBookIds + book.id) }
        }
    }
}
