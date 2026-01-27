package nl.rhaydus.softcover.feature.book.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.core.presentation.toad.ToadViewModel
import nl.rhaydus.softcover.feature.book.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.feature.book.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.reading.domain.usecase.GetCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val fetchBookByIdUseCase: FetchBookByIdUseCase,
    private val updateBookEditionUseCase: UpdateBookEditionUseCase,
    private val updateBookProgress: UpdateBookProgress,
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase,
    @param:Named("mainDispatcher") private val mainDispatcher: CoroutineDispatcher,
) : ToadViewModel<BookDetailUiState, BookDetailEvent>(
    initialState = BookDetailUiState()
) {
    override val dependencies: ActionDependencies = BookDetailDependencies(
        coroutineScope = viewModelScope,
        mainDispatcher = mainDispatcher,
        fetchBookByIdUseCase = fetchBookByIdUseCase,
        updateBookEditionUseCase = updateBookEditionUseCase,
        updateBookProgress = updateBookProgress,
    )

    // TODO: Move this to collector flow
    init {
        viewModelScope.launch(mainDispatcher) {
            getCurrentlyReadingBooksUseCase().collectLatest { books ->
                val matchingBook =
                    books.find { it.id == state.value.book?.id } ?: return@collectLatest

                scope.setState {
                    copy(book = matchingBook)
                }
            }
        }
    }

    fun runAction(action: BookDetailAction) = dispatch(action)
}