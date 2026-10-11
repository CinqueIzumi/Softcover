package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.richtext.toReviewDocument
import nl.rhaydus.softcover.core.uibinding.verdict.draftReview
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal data class OnSaveVerdictAction(
    private val book: Book,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val draft = scope.currentState.verdictSheet ?: return

        dependencies.saveBookVerdictUseCase(
            book = book,
            rating = draft.rating,
            review = draft.draftReview().toReviewDocument(),
            hasSpoilers = draft.spoilers.checked,
        )
            .onSuccess {
                scope.sendEvent(BookDetailEvent.MutationSucceededEvent(feedback = Feedback.Routine))
            }
            .onFailure { error ->
                AppLog.e("$error")

                scope.setState { it.copy(failedMutationBookIds = it.failedMutationBookIds + book.id) }
            }
    }
}
