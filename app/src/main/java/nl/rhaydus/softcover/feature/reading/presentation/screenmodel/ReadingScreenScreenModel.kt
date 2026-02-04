package nl.rhaydus.softcover.feature.reading.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadScreenModel
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.InitializeUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.RefreshUserBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.initializer.ReadingInitializer
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress

class ReadingScreenScreenModel(
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    private val updateBookProgressUseCase: UpdateBookProgressUseCase,
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val refreshUserBooksUseCase: RefreshUserBooksUseCase,
    private val updateBookEditionUseCase: UpdateBookEditionUseCase,
    private val updateBookProgress: UpdateBookProgress,
    private val initializeUserBooksUseCase: InitializeUserBooksUseCase,
    appDispatchers: AppDispatchers,
    flows: List<ReadingInitializer>,
) : ToadScreenModel<ReadingScreenUiState, ReadingScreenEvent, ReadingScreenDependencies, ReadingInitializer, ReadingLocalVariables>(
    initialState = ReadingScreenUiState(),
    initialLocalVariables = ReadingLocalVariables(),
    initializers = flows,
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
        initializeUserBooksUseCase = initializeUserBooksUseCase,
    )

    init {
        startInitializers()
    }

    fun runAction(action: ReadingAction) = dispatch(action)
}