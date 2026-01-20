package nl.rhaydus.softcover.feature.reading.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.presentation.toad.ToadViewModel
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.domain.usecase.GetCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.RefreshCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgressUtil
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ReadingScreenViewModel @Inject constructor(
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase,
    private val updateBookProgressUseCase: UpdateBookProgressUseCase,
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val refreshCurrentlyReadingBooksUseCase: RefreshCurrentlyReadingBooksUseCase,
    private val updateBookEditionUseCase: UpdateBookEditionUseCase,
    private val updateBookProgressUtil: UpdateBookProgressUtil,
    @param:Named("mainDispatcher") private val mainDispatcher: CoroutineDispatcher,
) : ToadViewModel<ReadingScreenUiState, ReadingScreenEvent>(
    initialState = ReadingScreenUiState()
) {
    override val dependencies = ReadingScreenDependencies(
        coroutineScope = viewModelScope,
        mainDispatcher = mainDispatcher,
        getCurrentlyReadingBooksUseCase = getCurrentlyReadingBooksUseCase,
        updateBookProgressUseCase = updateBookProgressUseCase,
        markBookAsReadUseCase = markBookAsReadUseCase,
        refreshCurrentlyReadingBooksUseCase = refreshCurrentlyReadingBooksUseCase,
        updateBookEditionUseCase = updateBookEditionUseCase,
        updateBookProgressUtil = updateBookProgressUtil,
    )

    // TODO: Ideally I'd want to have this in the same manner as the actions, to prevent modifying
    //  this file when expanding / removing
    init {
        scope.setState { copy(isLoading = true) }

        dependencies.launch {
            dependencies
                .getCurrentlyReadingBooksUseCase()
                .collectLatest { books: List<BookWithProgress> ->
                    scope.setState {
                        copy(
                            books = books,
                            isLoading = false,
                        )
                    }
                }
        }
    }


    fun runAction(action: ReadingAction) = dispatch(action)
}