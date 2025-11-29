package nl.rhaydus.softcover.feature.reading.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import nl.rhaydus.softcover.core.domain.exception.NoUserIdFoundException
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.domain.usecase.GetCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenUiEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCaseAsFlow
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReadingScreenViewModel @Inject constructor(
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase,
    getUserIdUseCaseAsFlow: GetUserIdUseCaseAsFlow,
) : ViewModel() {
    private val _loadingFlow = MutableStateFlow(true)
    private val _refreshTrigger = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    ).apply { tryEmit(Unit) }

    private val _booksFlow = combine(
        getUserIdUseCaseAsFlow(),
        _refreshTrigger
    ) { _, _ -> }.mapLatest { fetchCurrentlyReadingBooks() }

    val uiState = combine(
        _booksFlow,
        _loadingFlow
    ) { books: List<BookWithProgress>, isLoading: Boolean ->
        ReadingScreenUiState(
            books = books,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = ReadingScreenUiState(),
        started = SharingStarted.Lazily
    )

    fun onEvent(event: ReadingScreenUiEvent) {
        when (event) {
            ReadingScreenUiEvent.Refresh -> handleRefresh()
        }
    }

    private fun handleRefresh() {
        _refreshTrigger.tryEmit(Unit)
    }

    private suspend fun fetchCurrentlyReadingBooks(): List<BookWithProgress> {
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

        setLoadingState(isLoading = false)

        return books
    }

    private fun setLoadingState(isLoading: Boolean) {
        _loadingFlow.update { isLoading }
    }
}