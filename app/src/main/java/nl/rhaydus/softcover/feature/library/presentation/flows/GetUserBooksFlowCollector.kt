package nl.rhaydus.softcover.feature.library.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.viewmodel.LibraryDependencies

class GetUserBooksFlowCollector : LibraryFlowCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent>,
        dependencies: LibraryDependencies,
    ) {
        dependencies.getUserBooksAsFlowUseCase().collectLatest { books: List<Book> ->
            val wantToReadBooks = books.filter { it.status == BookStatus.WantToRead }
            val currentlyReadingBooks = books.filter { it.status == BookStatus.Reading }
            val readBooks = books.filter { it.status == BookStatus.Read }
            val dnfBooks = books.filter { it.status == BookStatus.DidNotFinish }

            scope.setState {
                copy(
                    allBooks = books,
                    wantToReadBooks = wantToReadBooks,
                    currentlyReadingBooks = currentlyReadingBooks,
                    readBooks = readBooks,
                    dnfBooks = dnfBooks
                )
            }
        }
    }
}