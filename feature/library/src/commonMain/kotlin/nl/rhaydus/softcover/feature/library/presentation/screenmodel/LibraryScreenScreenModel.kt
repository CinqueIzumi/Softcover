package nl.rhaydus.softcover.feature.library.presentation.screenmodel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetSortedAllUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetSortedBooksByStatusUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ReorderShelfBooksUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ObserveAllBookDeadlinesUseCase
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.AddBookToListUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.RemoveBookFromListUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.ReorderListBooksUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.SetListRankedUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibraryGridLayoutAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibrarySortSettingsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibraryTabOrderAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetShelfSwipeEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLibraryGridLayoutUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLibrarySortUseCase
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.collector.LibraryCollector
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ToadScreenModel

internal class LibraryScreenScreenModel(
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
    private val getShelfSwipeEnabledAsFlowUseCase: GetShelfSwipeEnabledAsFlowUseCase,
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val markBookAsReadingUseCase: MarkBookAsReadingUseCase,
    private val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    private val removeBookFromLibraryUseCase: RemoveBookFromLibraryUseCase,
    private val reorderShelfBooksUseCase: ReorderShelfBooksUseCase,
    private val addBookToListUseCase: AddBookToListUseCase,
    private val removeBookFromListUseCase: RemoveBookFromListUseCase,
    private val reorderListBooksUseCase: ReorderListBooksUseCase,
    private val setListRankedUseCase: SetListRankedUseCase,
    appDispatchers: AppDispatchers,
    flows: List<LibraryCollector>,
) : ToadScreenModel<LibraryUiState, LibraryEvent, LibraryDependencies, LibraryCollector, LibraryLocalVariables>(
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
        getShelfSwipeEnabledAsFlowUseCase = getShelfSwipeEnabledAsFlowUseCase,
        markBookAsReadUseCase = markBookAsReadUseCase,
        markBookAsReadingUseCase = markBookAsReadingUseCase,
        markBookAsWantToReadUseCase = markBookAsWantToReadUseCase,
        removeBookFromLibraryUseCase = removeBookFromLibraryUseCase,
        reorderShelfBooksUseCase = reorderShelfBooksUseCase,
        addBookToListUseCase = addBookToListUseCase,
        removeBookFromListUseCase = removeBookFromListUseCase,
        reorderListBooksUseCase = reorderListBooksUseCase,
        setListRankedUseCase = setListRankedUseCase,
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
