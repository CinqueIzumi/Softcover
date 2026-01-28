package nl.rhaydus.softcover.feature.library.presentation.viewmodel

import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.presentation.toad.ToadViewModel
import nl.rhaydus.softcover.feature.library.domain.usecase.GetUserBooksAsFlowUseCase
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

class LibraryScreenViewModel(
    private val getUserBooksAsFlowUseCase: GetUserBooksAsFlowUseCase,
    private val appDispatchers: AppDispatchers,
) : ToadViewModel<LibraryUiState, LibraryEvent>(
    initialState = LibraryUiState()
) {
    override val dependencies = LibraryDependencies(
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    // TODO: Add collector flow to arch...
    init {
        screenModelScope.launch(appDispatchers.main) {
            getUserBooksAsFlowUseCase().collectLatest { books: List<Book> ->
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

    fun runAction(action: LibraryAction) = dispatch(action = action)
}