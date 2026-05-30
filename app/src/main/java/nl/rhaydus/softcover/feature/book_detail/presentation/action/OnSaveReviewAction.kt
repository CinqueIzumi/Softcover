package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.presentation.util.htmlToAnnotatedString
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import timber.log.Timber

class OnSaveReviewAction(
    private val book: Book,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val body: String = scope.currentState.reviewEditorBody.trim()
        val hasSpoilers: Boolean = scope.currentState.reviewEditorHasSpoilers

        scope.setState { it.copy(showReviewSheet = false) }

        // Skip the round-trip when nothing meaningful changed — an untouched empty editor on a book
        // that has no review would otherwise fire a pointless "clear" mutation. Both the body and the
        // spoiler flag are user-editable, so a spoiler-only toggle must still save.
        val existingReview: String = book.userBook?.review
            ?.let { htmlToAnnotatedString(html = it).text.trim() }
            .orEmpty()

        val existingHasSpoilers: Boolean = book.userBook?.reviewHasSpoilers ?: false

        if (body == existingReview && hasSpoilers == existingHasSpoilers) return

        dependencies.updateBookReviewUseCase(
            book = book,
            body = body,
            hasSpoilers = hasSpoilers,
        ).onFailure { error ->
            Timber.e("$error")

            scope.setState { it.copy(failedMutationBookIds = it.failedMutationBookIds + book.id) }
        }
    }
}
