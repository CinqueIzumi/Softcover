package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.withBook
import nl.rhaydus.toad.ActionScope

internal class UserBooksFlowCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.book?.id }
            .distinctUntilChanged()
            .flatMapLatest { bookId ->
                bookId?.let { id -> dependencies.observeUserBookUseCase(id = id) } ?: emptyFlow()
            }.collectLatest { localBook ->
                localBook ?: return@collectLatest

                scope.setState { state ->
                    val currentBook = state.book ?: return@setState state
                    val overlaid = currentBook.overlay(localBook)

                    if (overlaid == currentBook) state else state.withBook(book = overlaid)
                }
            }
    }

    private fun Book.overlay(localBook: Book): Book {
        val ownedEditionIds = localBook.editions
            .filter { it.owned }
            .mapTo(mutableSetOf()) { it.id }

        val overlaidEditions = editions.map { edition ->
            val shouldBeOwned = edition.id in ownedEditionIds
            if (edition.owned == shouldBeOwned) edition else edition.copy(owned = shouldBeOwned)
        }

        return copy(
            userBook = localBook.userBook,
            userBookRead = localBook.userBookRead,
            editions = overlaidEditions,
        )
    }
}
