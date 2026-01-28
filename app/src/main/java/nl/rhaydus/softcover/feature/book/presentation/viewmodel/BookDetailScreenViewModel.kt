package nl.rhaydus.softcover.feature.book.presentation.viewmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadViewModel
import nl.rhaydus.softcover.feature.book.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.feature.book.domain.usecase.UpdateBookStatusUseCase
import nl.rhaydus.softcover.feature.book.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.flows.BookDetailFlowCollector
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
    flows: List<BookDetailFlowCollector>,
    appDispatchers: AppDispatchers,
) : ToadViewModel<BookDetailUiState, BookDetailEvent, BookDetailDependencies, BookDetailFlowCollector>(
    initialState = BookDetailUiState(),
    initialFlowCollectors = flows,
) {
    override val dependencies: BookDetailDependencies = BookDetailDependencies(
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
        fetchBookByIdUseCase = fetchBookByIdUseCase,
        updateBookEditionUseCase = updateBookEditionUseCase,
        updateBookProgress = updateBookProgress,
        updateBookStatusUseCase = updateBookStatusUseCase,
        getUserBooksAsFlowUseCase = getUserBooksAsFlowUseCase,
    )

    init {
        startFlowCollectors()
    }

    fun runAction(action: BookDetailAction) = dispatch(action)
}