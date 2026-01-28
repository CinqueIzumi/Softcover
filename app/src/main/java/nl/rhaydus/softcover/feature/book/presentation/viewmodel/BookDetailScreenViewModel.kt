package nl.rhaydus.softcover.feature.book.presentation.viewmodel

import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.core.presentation.toad.ToadViewModel
import nl.rhaydus.softcover.feature.book.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.feature.book.domain.usecase.UpdateBookStatusUseCase
import nl.rhaydus.softcover.feature.book.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.library.domain.usecase.GetUserBooksAsFlowUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress

class BookDetailScreenViewModel(
    private val fetchBookByIdUseCase: FetchBookByIdUseCase,
    private val updateBookEditionUseCase: UpdateBookEditionUseCase,
    private val updateBookProgress: UpdateBookProgress,
    private val getUserBooksAsFlowUseCase: GetUserBooksAsFlowUseCase,
    private val updateBookStatusUseCase: UpdateBookStatusUseCase,
    appDispatchers: AppDispatchers,
) : ToadViewModel<BookDetailUiState, BookDetailEvent>(
    initialState = BookDetailUiState()
) {
    override val dependencies: ActionDependencies = BookDetailDependencies(
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
        fetchBookByIdUseCase = fetchBookByIdUseCase,
        updateBookEditionUseCase = updateBookEditionUseCase,
        updateBookProgress = updateBookProgress,
        updateBookStatusUseCase = updateBookStatusUseCase,
    )

    // TODO: Move this to collector flow
    init {
        screenModelScope.launch(appDispatchers.main) {
            getUserBooksAsFlowUseCase().collectLatest { books ->
                val matchingBook =
                    books.find { it.id == state.value.book?.id } ?: return@collectLatest

                scope.setState {
                    copy(book = matchingBook)
                }
            }
        }
    }

    fun runAction(action: BookDetailAction) = dispatch(action)
}