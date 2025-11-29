package nl.rhaydus.softcover.feature.reading.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.domain.exception.NoUserIdFoundException
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.domain.usecase.GetCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenUiEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCaseAsFlow
import javax.inject.Inject

@HiltViewModel
class ReadingScreenViewModel @Inject constructor(
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase,
    getUserIdUseCaseAsFlow: GetUserIdUseCaseAsFlow,
) : ViewModel() {
    private val _loadingFlow = MutableStateFlow(true)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = combine(
        getUserIdUseCaseAsFlow().mapLatest { fetchCurrentlyReadingBooks() },
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
        viewModelScope.launch {
            fetchCurrentlyReadingBooks()
        }
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