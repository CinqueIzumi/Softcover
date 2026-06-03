package nl.rhaydus.softcover.feature.book_detail.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

internal class UserBooksFlowCollector : BookDetailInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        combine(
            dependencies.getAllUserBooksUseCase(),
            scope.state.map { it.book?.id }.distinctUntilChanged(),
        ) { books, bookId ->
            bookId?.let { id -> books.find { it.id == id } }
        }.collectLatest { localBook ->
            localBook ?: return@collectLatest

            scope.setState { state ->
                val currentBook = state.book ?: return@setState state

                state.copy(book = currentBook.overlay(localBook))
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
