package nl.rhaydus.softcover.feature.library.presentation.screenmodel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadScreenModel
import nl.rhaydus.softcover.feature.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetSortedAllUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetSortedBooksByStatusUseCase
import nl.rhaydus.softcover.feature.library.domain.usecase.RefreshLibraryUseCase
import nl.rhaydus.softcover.feature.deadlines.domain.usecase.ObserveAllBookDeadlinesUseCase
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.flows.LibraryInitializer
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetLibraryGridLayoutAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetLibrarySortSettingsAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetLibraryTabOrderAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibraryGridLayoutUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibrarySortUseCase

class LibraryScreenScreenModel(
    private val getSortedAllUserBooksUseCase: GetSortedAllUserBooksUseCase,
    private val getSortedBooksByStatusUseCase: GetSortedBooksByStatusUseCase,
    private val refreshLibraryUseCase: RefreshLibraryUseCase,
    private val getAllUserListsUseCase: GetAllUserListsUseCase,
    private val getLibraryGridLayoutAsFlowUseCase: GetLibraryGridLayoutAsFlowUseCase,
    private val setLibraryGridLayoutUseCase: SetLibraryGridLayoutUseCase,
    private val getLibrarySortSettingsAsFlowUseCase: GetLibrarySortSettingsAsFlowUseCase,
    private val setLibrarySortUseCase: SetLibrarySortUseCase,
    private val observeAllBookDeadlinesUseCase: ObserveAllBookDeadlinesUseCase,
    private val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    private val getEnabledStatusCodesAsFlowUseCase: GetEnabledStatusCodesAsFlowUseCase,
    private val getEnabledListIdsAsFlowUseCase: GetEnabledListIdsAsFlowUseCase,
    private val getLibraryTabOrderAsFlowUseCase: GetLibraryTabOrderAsFlowUseCase,
    appDispatchers: AppDispatchers,
    flows: List<LibraryInitializer>,
) : ToadScreenModel<LibraryUiState, LibraryEvent, LibraryDependencies, LibraryInitializer, LibraryLocalVariables>(
    initialState = LibraryUiState(),
    initialLocalVariables = LibraryLocalVariables(),
    initializers = flows,
) {
    override val dependencies = LibraryDependencies(
        getSortedAllUserBooksUseCase = getSortedAllUserBooksUseCase,
        getSortedBooksByStatusUseCase = getSortedBooksByStatusUseCase,
        refreshLibraryUseCase = refreshLibraryUseCase,
        getAllUserListsUseCase = getAllUserListsUseCase,
        getLibraryGridLayoutAsFlowUseCase = getLibraryGridLayoutAsFlowUseCase,
        setLibraryGridLayoutUseCase = setLibraryGridLayoutUseCase,
        getLibrarySortSettingsAsFlowUseCase = getLibrarySortSettingsAsFlowUseCase,
        setLibrarySortUseCase = setLibrarySortUseCase,
        observeAllBookDeadlinesUseCase = observeAllBookDeadlinesUseCase,
        getDateStyleAsFlowUseCase = getDateStyleAsFlowUseCase,
        getEnabledStatusCodesAsFlowUseCase = getEnabledStatusCodesAsFlowUseCase,
        getEnabledListIdsAsFlowUseCase = getEnabledListIdsAsFlowUseCase,
        getLibraryTabOrderAsFlowUseCase = getLibraryTabOrderAsFlowUseCase,
        mainDispatcher = appDispatchers.main,
        defaultDispatcher = appDispatchers.default,
        coroutineScope = screenModelScope,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    val headerScrollState: TopAppBarState = TopAppBarState(
        initialHeightOffsetLimit = -Float.MAX_VALUE,
        initialHeightOffset = 0f,
        initialContentOffset = 0f,
    )

    init {
        startInitializers()
    }

    fun runAction(action: LibraryAction) = dispatch(action = action)
}
