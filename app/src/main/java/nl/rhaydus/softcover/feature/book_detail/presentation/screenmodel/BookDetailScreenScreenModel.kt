package nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadScreenModel
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.flows.BookDetailInitializer
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.books.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.SetEditionAsOwnedUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetDateStyleAsFlowUseCase

class BookDetailScreenScreenModel(
    private val fetchBookByIdUseCase: FetchBookByIdUseCase,
    private val updateBookEditionUseCase: UpdateBookEditionUseCase,
    private val updateBookProgress: UpdateBookProgress,
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    private val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    private val markBookAsReadingUseCase: MarkBookAsReadingUseCase,
    private val removeBookFromLibraryUseCase: RemoveBookFromLibraryUseCase,
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    private val setEditionAsOwnedUseCase: SetEditionAsOwnedUseCase,
    flows: List<BookDetailInitializer>,
    appDispatchers: AppDispatchers,
) : ToadScreenModel<BookDetailUiState, BookDetailEvent, BookDetailDependencies, BookDetailInitializer, BookDetailLocalVariables>(
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
        getAllUserBooksUseCase = getAllUserBooksUseCase,
        markBookAsWantToReadUseCase = markBookAsWantToReadUseCase,
        markBookAsReadingUseCase = markBookAsReadingUseCase,
        removeBookFromLibraryUseCase = removeBookFromLibraryUseCase,
        markBookAsReadUseCase = markBookAsReadUseCase,
        getDateStyleAsFlowUseCase = getDateStyleAsFlowUseCase,
        setEditionAsOwnedUseCase = setEditionAsOwnedUseCase,
    )

    init {
        startInitializers()
    }

    fun runAction(action: BookDetailAction) = dispatch(action)
}