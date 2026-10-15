package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.collections.immutable.toPersistentSet
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLens
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.withBook
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
                    it.copy(failedMutationBookIds = it.failedMutationBookIds.toPersistentSet().add(book.id))
                }
            }
            .onSuccess {
                scope.setState { state ->
                    state.copy(selectedLens = BookDetailLens.THE_BOOK).withBook(
                        book = state.book?.copy(userBook = null),
                    )
                }
            }
    }
}
