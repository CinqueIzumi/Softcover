package nl.rhaydus.softcover.feature.reading.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.domain.usecase.GetCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.RefreshCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenUiEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ProgressTab
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import timber.log.Timber
import javax.inject.Inject

// TODO: When opening the edition sheet, can the active button be disabled already?
// TODO: Rather than refresh data after a query, maybe the data can be returned immediately from the queries?
// TODO: Ideally I'd want to update all error logging, preventing the app from crashing in case something unexpected goes wrong...
// TODO: Clean-up all files, code style guide, feels messy
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReadingScreenViewModel @Inject constructor(
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase,
    private val updateBookProgressUseCase: UpdateBookProgressUseCase,
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val refreshCurrentlyReadingBooksUseCase: RefreshCurrentlyReadingBooksUseCase,
    private val updateBookEditionUseCase: UpdateBookEditionUseCase,
) : ViewModel() {
    private val _progressTabFlow = MutableStateFlow(ProgressTab.PAGE)
    private val _loadingFlow = MutableStateFlow(true)
    private val _bookToUpdateFlow = MutableStateFlow<BookWithProgress?>(null)
    private val _booksFlow = MutableStateFlow<List<BookWithProgress>>(emptyList())
    private val _showProgressSheet = MutableStateFlow(false)
    private val _showEditionSheet = MutableStateFlow(false)

    // TODO: Implement custom combine functions, as casting args feels error-prone...
    val uiState = combine(
        _booksFlow,
        _loadingFlow,
        _bookToUpdateFlow,
        _progressTabFlow,
        _showProgressSheet,
        _showEditionSheet,
    ) { args ->
        val books = args[0] as List<BookWithProgress>
        val isLoading = args[1] as Boolean
        val bookToUpdate = args[2] as BookWithProgress?
        val tab = args[3] as ProgressTab
        val showProgressSheet = args[4] as Boolean
        val showEditionSheet = args[5] as Boolean

        ReadingScreenUiState(
            books = books,
            isLoading = isLoading,
            bookToUpdate = bookToUpdate,
            progressTab = tab,
            showProgressSheet = showProgressSheet,
            showEditionSheet = showEditionSheet,
        )
    }.onStart {
        Timber.d("-=- initializing collectors!")
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
                handleOnProgressTabClick(newTab = event.newProgressTab)
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

    private fun handleOnNewEditionSaveClick(edition: BookEdition) {
        val bookToUpdate = _bookToUpdateFlow.value ?: return

        viewModelScope.launch {
            // TODO: Error handling should be done throughout the app, see docs with different error codes
            updateBookEditionUseCase(
                userBookId = bookToUpdate.userBookId,
                newEditionId = edition.id,
            ).onFailure {
                Timber.e("Something went wrong updating book edition! $it")
            }

            dismissAllSheets()
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

    private fun handleOnMarkBookAsReadClick(book: BookWithProgress) {
        viewModelScope.launch {
            markBookAsRead(bookWithProgress = book)
        }
    }

    private fun handleOnUpdatePageProgressClick(newPage: String) {
        val bookToUpdate = _bookToUpdateFlow.value ?: return

        val newPageValue = newPage.toIntOrNull() ?: 0

        viewModelScope.launch {
            if (newPageValue == bookToUpdate.currentEdition.pages) {
                markBookAsRead(bookWithProgress = bookToUpdate)
            } else {
                updateBookWithPage(
                    bookWithProgress = bookToUpdate,
                    page = newPageValue,
                )
            }
        }
    }

    private fun handleOnUpdatePercentageProgressClick(newPercentage: String) {
        val bookToUpdate = _bookToUpdateFlow.value ?: return

        val newPercentageValue = newPercentage.toDoubleOrNull() ?: 0.0

        val newPageValue: Int =
            ((newPercentageValue / 100) * (bookToUpdate.currentEdition.pages ?: 0)).toInt()

        handleOnUpdatePageProgressClick(newPage = newPageValue.toString())
    }

    private fun handleOnProgressTabClick(newTab: ProgressTab) {
        setProgressTab(tab = newTab)
    }

    private suspend fun markBookAsRead(bookWithProgress: BookWithProgress) {
        markBookAsReadUseCase(book = bookWithProgress).onFailure {
            Timber.e("Error while marking book as read! $it")
        }

        dismissAllSheets()
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

        dismissAllSheets()
    }

    private fun handleDismissProgressSheet() = dismissAllSheets()

    private fun handleDismissEditionBottomSheet() = dismissAllSheets()

    private fun handleRefresh() {
        viewModelScope.launch {
            setLoadingState(isLoading = true)

            refreshCurrentlyReadingBooksUseCase().onFailure {
                Timber.e("Something went wrong refreshing currently reading books! $it")
            }

            setLoadingState(isLoading = false)
        }
    }

    private fun handleOnUpgradeProgressClick(book: BookWithProgress) {
        setBookForSheet(book = book)

        setShowProgressSheet(show = true)
    }

    private fun handleOnShowEditionSheetClick(book: BookWithProgress) {
        setBookForSheet(book = book)

        setShowEditionSheet(show = true)
    }

    private fun dismissAllSheets() {
        setShowEditionSheet(show = false)
        setShowProgressSheet(show = false)

        setBookForSheet(book = null)
    }

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

    private fun setProgressTab(tab: ProgressTab) {
        _progressTabFlow.update { tab }
    }

    private fun setBooksWithProgress(books: List<BookWithProgress>) {
        _booksFlow.update { books }
    }
}