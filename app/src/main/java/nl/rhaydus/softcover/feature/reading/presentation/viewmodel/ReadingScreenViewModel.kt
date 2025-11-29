package nl.rhaydus.softcover.feature.reading.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import nl.rhaydus.softcover.core.domain.exception.NoUserIdFoundException
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.domain.usecase.GetCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCaseAsFlow
import javax.inject.Inject

@HiltViewModel
class ReadingScreenViewModel @Inject constructor(
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase,
    getUserIdUseCaseAsFlow: GetUserIdUseCaseAsFlow,
) : ViewModel() {
    // TODO: Implement something like a loading state or something among those lines
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = getUserIdUseCaseAsFlow()
        .mapLatest { fetchCurrentlyReadingBooks() }
        .map { books -> ReadingScreenUiState(books = books) }
        .stateIn(
            scope = viewModelScope,
            initialValue = ReadingScreenUiState(),
            started = SharingStarted.Lazily
        )

    private suspend fun fetchCurrentlyReadingBooks(): List<BookWithProgress> {
        return getCurrentlyReadingBooksUseCase().fold(
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
    }
}