package nl.rhaydus.softcover.feature.reading.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.domain.exception.NoUserIdFoundException
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.domain.usecase.GetCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenUiEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCaseAsFlow
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReadingScreenViewModel @Inject constructor(
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase,
    private val updateBookProgressUseCase: UpdateBookProgressUseCase,
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val getUserIdUseCaseAsFlow: GetUserIdUseCaseAsFlow,
) : ViewModel() {
    private val _loadingFlow = MutableStateFlow(true)
    private val _bookToUpdateFlow = MutableStateFlow<BookWithProgress?>(null)
    private val _booksFlow = MutableStateFlow<List<BookWithProgress>>(emptyList())

    val uiState = combine(
        _booksFlow,
        _loadingFlow,
        _bookToUpdateFlow
    ) { books: List<BookWithProgress>, isLoading: Boolean, bookToUpdate: BookWithProgress? ->
        ReadingScreenUiState(
            books = books,
            isLoading = isLoading,
            bookToUpdate = bookToUpdate,
        )
    }.onStart {
        initializeCollectors()
    }.stateIn(
        scope = viewModelScope,
        initialValue = ReadingScreenUiState(),
        started = SharingStarted.Lazily
    )

    fun onEvent(event: ReadingScreenUiEvent) {
        when (event) {
            ReadingScreenUiEvent.Refresh -> handleRefresh()
            ReadingScreenUiEvent.DismissProgressSheet -> handleDismissProgressSheet()

            is ReadingScreenUiEvent.OnSetProgressClick -> handleOnUpgradeProgressClick(book = event.book)
            is ReadingScreenUiEvent.OnUpdateProgressClick -> handleOnUpdateProgressClick(newPage = event.newPage)
        }
    }

    private fun initializeCollectors() {
        viewModelScope.launch {
            getUserIdUseCaseAsFlow().collect { handleRefresh() }
        }
    }

    private fun handleOnUpdateProgressClick(newPage: String) {
        val bookToUpdate = _bookToUpdateFlow.value ?: return

        val newPageValue = newPage.toIntOrNull() ?: 0

        viewModelScope.launch {
            if (newPageValue == bookToUpdate.book.totalPages) {
                markBookAsRead(bookWithProgress = bookToUpdate)
            } else {
                updateBookWithPage(
                    bookWithProgress = bookToUpdate,
                    page = newPageValue,
                )
            }
        }
    }

    private suspend fun markBookAsRead(bookWithProgress: BookWithProgress) {
        markBookAsReadUseCase(book = bookWithProgress)

        setBookForProgressSheet(book = null)

        fetchCurrentlyReadingBooks()
    }

    private suspend fun updateBookWithPage(
        bookWithProgress: BookWithProgress,
        page: Int,
    ) {
        val updatedBook = updateBookProgressUseCase(
            book = bookWithProgress,
            newPage = page,
        ).getOrDefault(bookWithProgress)

        setBookForProgressSheet(book = null)

        val currentBooks = _booksFlow.firstOrNull() ?: return

        val updatedBooks = currentBooks.map { book ->
            if (book.userProgressId == updatedBook.userProgressId) {
                updatedBook
            } else {
                book
            }
        }

        setBooksWithProgress(books = updatedBooks)
    }

    private fun handleDismissProgressSheet() = setBookForProgressSheet(book = null)

    private fun handleRefresh() = fetchCurrentlyReadingBooks()

    private fun handleOnUpgradeProgressClick(book: BookWithProgress) {
        setBookForProgressSheet(book = book)
    }

    private fun fetchCurrentlyReadingBooks() {
        viewModelScope.launch {
            setLoadingState(isLoading = true)

            val books = getCurrentlyReadingBooksUseCase().fold(
                onSuccess = { it },
                onFailure = { failure ->
                    val message = if (failure is NoUserIdFoundException) {
                        "Without a valid API key the user's data can not be initialized"
                    } else {
                        "Something went wrong while trying to fetch the currently reading books."
                    }

                    SnackBarManager.showSnackbar(title = message)

                    emptyList()
                }
            )

            setBooksWithProgress(books = books)

            setLoadingState(isLoading = false)
        }
    }

    private fun setBookForProgressSheet(book: BookWithProgress?) {
        _bookToUpdateFlow.update { book }
    }

    private fun setLoadingState(isLoading: Boolean) {
        _loadingFlow.update { isLoading }
    }

    private fun setBooksWithProgress(books: List<BookWithProgress>) {
        _booksFlow.update { books }
    }
}