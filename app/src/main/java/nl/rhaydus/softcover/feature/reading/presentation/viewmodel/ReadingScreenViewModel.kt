package nl.rhaydus.softcover.feature.reading.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.util.combine
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.domain.usecase.GetCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.RefreshCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.feature.reading.presentation.ProgressSheetTab
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenUiEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReadingScreenViewModel @Inject constructor(
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase,
    private val updateBookProgressUseCase: UpdateBookProgressUseCase,
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val refreshCurrentlyReadingBooksUseCase: RefreshCurrentlyReadingBooksUseCase,
    private val updateBookEditionUseCase: UpdateBookEditionUseCase,
) : ViewModel() {
    private val _progressSheetTabFlow = MutableStateFlow(ProgressSheetTab.PAGE)
    private val _loadingFlow = MutableStateFlow(true)
    private val _bookToUpdateFlow = MutableStateFlow<BookWithProgress?>(null)
    private val _booksFlow = MutableStateFlow<List<BookWithProgress>>(emptyList())
    private val _showProgressSheet = MutableStateFlow(false)
    private val _showEditionSheet = MutableStateFlow(false)

    val uiState = combine(
        _booksFlow,
        _loadingFlow,
        _bookToUpdateFlow,
        _progressSheetTabFlow,
        _showProgressSheet,
        _showEditionSheet,
    ) {
            books: List<BookWithProgress>,
            isLoading: Boolean,
            bookToUpdate: BookWithProgress?,
            tab: ProgressSheetTab,
            showProgressSheet: Boolean,
            showEditionSheet: Boolean,
        ->

        ReadingScreenUiState(
            books = books,
            isLoading = isLoading,
            bookToUpdate = bookToUpdate,
            progressSheetTab = tab,
            showProgressSheet = showProgressSheet,
            showEditionSheet = showEditionSheet,
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
            ReadingScreenUiEvent.DismissEditionSheet -> handleDismissEditionBottomSheet()

            is ReadingScreenUiEvent.OnShowProgressSheetClick -> {
                handleOnUpgradeProgressClick(book = event.book)
            }

            is ReadingScreenUiEvent.OnShowEditionSheetClick -> {
                handleOnShowEditionSheetClick(book = event.book)
            }

            is ReadingScreenUiEvent.OnProgressTabClick -> {
                handleOnProgressTabClick(newTab = event.newProgressSheetTab)
            }

            is ReadingScreenUiEvent.OnUpdatePageProgressClick -> {
                handleOnUpdatePageProgressClick(newPage = event.newPage)
            }

            is ReadingScreenUiEvent.OnUpdatePercentageProgressClick -> {
                handleOnUpdatePercentageProgressClick(newPercentage = event.newProgress)
            }

            is ReadingScreenUiEvent.OnMarkBookAsReadClick -> {
                handleOnMarkBookAsReadClick(book = event.book)
            }

            is ReadingScreenUiEvent.OnNewEditionSaveClick -> {
                handleOnNewEditionSaveClick(edition = event.edition)
            }
        }
    }

    // region Event handlers
    private fun handleOnNewEditionSaveClick(edition: BookEdition) {
        val bookToUpdate = _bookToUpdateFlow.value ?: return

        updateBookEdition(
            book = bookToUpdate,
            edition = edition
        )
    }

    private fun handleOnMarkBookAsReadClick(book: BookWithProgress) {
        viewModelScope.launch {
            markBookAsRead(bookWithProgress = book)
        }
    }

    private fun handleOnUpdatePageProgressClick(newPage: String) {
        val bookToUpdate = _bookToUpdateFlow.value ?: return

        val newPageValue = newPage.toIntOrNull() ?: 0

        updateBookProgress(
            book = bookToUpdate,
            newPage = newPageValue,
        )
    }

    private fun handleOnUpdatePercentageProgressClick(newPercentage: String) {
        val bookToUpdate = _bookToUpdateFlow.value ?: return

        val newPercentageValue = newPercentage.toDoubleOrNull() ?: 0.0

        val newPageValue: Int =
            ((newPercentageValue / 100) * (bookToUpdate.currentEdition.pages ?: 0)).toInt()

        updateBookProgress(book = bookToUpdate, newPage = newPageValue)
    }

    private fun handleOnProgressTabClick(newTab: ProgressSheetTab) {
        setProgressTab(tab = newTab)
    }

    private fun handleDismissProgressSheet() = dismissProgressSheet()

    private fun handleDismissEditionBottomSheet() = dismissEditionSheet()

    private fun handleRefresh() =refreshCurrentlyReadingBooks()

    private fun handleOnUpgradeProgressClick(book: BookWithProgress) {
        setBookForSheet(book = book)

        setShowProgressSheet(show = true)
    }

    private fun handleOnShowEditionSheetClick(book: BookWithProgress) {
        setBookForSheet(book = book)

        setShowEditionSheet(show = true)
    }
    // endregion

    // region Helper methods
    private fun updateBookEdition(
        book: BookWithProgress,
        edition: BookEdition,
    ) {
        viewModelScope.launch {
            setLoadingState(isLoading = true)

            updateBookEditionUseCase(
                userBookId = book.userBookId,
                newEditionId = edition.id,
            ).onFailure {
                Timber.e("Something went wrong updating book edition! $it")
            }

            setLoadingState(isLoading = false)
        }

        dismissEditionSheet()
    }

    private fun updateBookProgress(
        book: BookWithProgress,
        newPage: Int,
    ) {
        viewModelScope.launch {
            setLoadingState(isLoading = true)

            if (newPage == book.currentEdition.pages) {
                markBookAsRead(bookWithProgress = book)
            } else {
                updateBookWithPage(
                    bookWithProgress = book,
                    page = newPage,
                )
            }

            setLoadingState(isLoading = false)
        }

        dismissProgressSheet()
    }

    private suspend fun updateBookWithPage(
        bookWithProgress: BookWithProgress,
        page: Int,
    ) {
        updateBookProgressUseCase(
            book = bookWithProgress,
            newPage = page,
        ).onFailure {
            Timber.e("Something went wrong updating book progress! $it")
        }
    }

    private fun refreshCurrentlyReadingBooks() {
        viewModelScope.launch {
            setLoadingState(isLoading = true)

            refreshCurrentlyReadingBooksUseCase().onFailure {
                Timber.e("Something went wrong refreshing currently reading books! $it")
            }

            setLoadingState(isLoading = false)
        }
    }

    private suspend fun markBookAsRead(bookWithProgress: BookWithProgress) {
        markBookAsReadUseCase(book = bookWithProgress).onFailure {
            Timber.e("Error while marking book as read! $it")
        }
    }

    private fun initializeCollectors() {
        setLoadingState(isLoading = true)

        viewModelScope.launch {
            launch {
                getCurrentlyReadingBooksUseCase().collectLatest { books ->
                    setBooksWithProgress(books = books)

                    setLoadingState(isLoading = false)
                }
            }
        }
    }

    private fun dismissEditionSheet() {
        setShowEditionSheet(show = false)

        setBookForSheet(book = null)
    }

    private fun dismissProgressSheet() {
        setShowProgressSheet(show = false)

        setBookForSheet(book = null)
    }
    // endregion

    // region State management
    private fun setShowEditionSheet(show: Boolean) {
        _showEditionSheet.update { show }
    }

    private fun setShowProgressSheet(show: Boolean) {
        _showProgressSheet.update { show }
    }

    private fun setBookForSheet(book: BookWithProgress?) {
        _bookToUpdateFlow.update { book }
    }

    private fun setLoadingState(isLoading: Boolean) {
        _loadingFlow.update { isLoading }
    }

    private fun setProgressTab(tab: ProgressSheetTab) {
        _progressSheetTabFlow.update { tab }
    }

    private fun setBooksWithProgress(books: List<BookWithProgress>) {
        _booksFlow.update { books }
    }
    // endregion
}