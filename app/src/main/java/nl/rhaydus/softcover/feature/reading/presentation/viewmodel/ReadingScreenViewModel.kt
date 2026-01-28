package nl.rhaydus.softcover.feature.reading.presentation.viewmodel

import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ToadViewModel
import nl.rhaydus.softcover.feature.library.domain.usecase.RefreshUserBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.GetCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress

class ReadingScreenViewModel(
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase,
    private val updateBookProgressUseCase: UpdateBookProgressUseCase,
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val refreshUserBooksUseCase: RefreshUserBooksUseCase,
    private val updateBookEditionUseCase: UpdateBookEditionUseCase,
    private val updateBookProgress: UpdateBookProgress,
    private val appDispatchers: AppDispatchers,
) : ToadViewModel<ReadingScreenUiState, ReadingScreenEvent>(
    initialState = ReadingScreenUiState()
) {
    override val dependencies = ReadingScreenDependencies(
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
        getCurrentlyReadingBooksUseCase = getCurrentlyReadingBooksUseCase,
        updateBookProgressUseCase = updateBookProgressUseCase,
        markBookAsReadUseCase = markBookAsReadUseCase,
        refreshUserBooksUseCase = refreshUserBooksUseCase,
        updateBookEditionUseCase = updateBookEditionUseCase,
        updateBookProgress = updateBookProgress,
    )

    // TODO: Ideally I'd want to have this in the same manner as the actions, to prevent modifying
    //  this file when expanding / removing
    init {
        scope.setState { copy(isLoading = true) }

        dependencies.launch {
            dependencies
                .getCurrentlyReadingBooksUseCase()
                .collectLatest { books: List<Book> ->
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