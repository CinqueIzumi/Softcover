package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal data class OnSaveVerdictAction(
    private val book: Book,
    private val rating: Double?,
    private val review: ReviewDocument,
    private val hasSpoilers: Boolean,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.setState { it.copy(verdictSheetContext = null) }

        dependencies.saveBookVerdictUseCase(
            book = book,
            rating = rating,
            review = review,
            hasSpoilers = hasSpoilers,
        ).onFailure { error ->
            AppLog.e("$error")

            scope.setState { it.copy(failedMutationBookIds = it.failedMutationBookIds + book.id) }
        }
    }
}
