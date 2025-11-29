package nl.rhaydus.softcover.feature.reading.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.domain.usecase.GetCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.NoUserIdFoundException
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class ReadingScreenViewModel @Inject constructor(
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase,
) : ViewModel() {
    private val _booksFlow = MutableStateFlow<List<BookWithProgress>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = _booksFlow.mapLatest { books ->
        ReadingScreenUiState(books = books)
    }.onStart {
        initializeCurrentlyReadingBooks()
    }.stateIn(
        scope = viewModelScope,
        initialValue = ReadingScreenUiState(),
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(stopTimeout = 5.seconds)
    )

    private fun initializeCurrentlyReadingBooks() {
        viewModelScope.launch {
            val books: List<BookWithProgress> = getCurrentlyReadingBooksUseCase().fold(
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

            _booksFlow.update { books }
        }
    }
}