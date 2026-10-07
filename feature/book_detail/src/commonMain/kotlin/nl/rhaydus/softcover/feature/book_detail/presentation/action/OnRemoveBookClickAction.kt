package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLens
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnRemoveBookClickAction(val book: Book) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.currentLocalVariables.bookMutationJobs[book.id]?.cancel()

        dependencies.undoableBookRemover
            .remove(books = listOf(book))
            .onFailure { error ->
                AppLog.e("$error")

                scope.setState {
                    it.copy(failedMutationBookIds = it.failedMutationBookIds + book.id)
                }
            }
            .onSuccess {
                scope.setState { state ->
                    state.copy(
                        selectedLens = BookDetailLens.THE_BOOK,
                        book = state.book?.copy(
                            userBook = null,
                            userBookRead = null,
                        ),
                    )
                }
            }
    }
}
