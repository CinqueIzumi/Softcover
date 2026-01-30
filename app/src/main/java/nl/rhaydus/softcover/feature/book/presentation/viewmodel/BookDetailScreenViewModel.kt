package nl.rhaydus.softcover.feature.book.presentation.viewmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadViewModel
import nl.rhaydus.softcover.feature.book.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.feature.book.domain.usecase.UpdateBookStatusUseCase
import nl.rhaydus.softcover.feature.book.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.flows.BookDetailInitializer
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress

class BookDetailScreenViewModel(
    private val fetchBookByIdUseCase: FetchBookByIdUseCase,
    private val updateBookEditionUseCase: UpdateBookEditionUseCase,
    private val updateBookProgress: UpdateBookProgress,
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    private val updateBookStatusUseCase: UpdateBookStatusUseCase,
    flows: List<BookDetailInitializer>,
    appDispatchers: AppDispatchers,
) : ToadViewModel<BookDetailUiState, BookDetailEvent, BookDetailDependencies, BookDetailInitializer, BookDetailLocalVariables>(
    initialState = BookDetailUiState(),
    initialLocalVariables = BookDetailLocalVariables(),
    initializers = flows,
) {
    override val dependencies: BookDetailDependencies = BookDetailDependencies(
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
        fetchBookByIdUseCase = fetchBookByIdUseCase,
        updateBookEditionUseCase = updateBookEditionUseCase,
        updateBookProgress = updateBookProgress,
        updateBookStatusUseCase = updateBookStatusUseCase,
        getAllUserBooksUseCase = getAllUserBooksUseCase,
    )

    init {
        startInitializers()
    }

    fun runAction(action: BookDetailAction) = dispatch(action)
}