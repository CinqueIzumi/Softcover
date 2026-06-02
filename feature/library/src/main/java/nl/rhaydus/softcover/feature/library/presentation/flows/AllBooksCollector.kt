package nl.rhaydus.softcover.feature.library.presentation.flows

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.LibrarySortSettings
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

@OptIn(ExperimentalCoroutinesApi::class)
class AllBooksCollector : LibraryInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        // SQL ORDER BY does the sorting — flatMapLatest re-subscribes whenever the persisted
        // sort settings for the All tab change, so the DAO emits an already-sorted list and the
        // UI has no main-thread work to do.
        val sortedBooksFlow = dependencies.getLibrarySortSettingsAsFlowUseCase()
            .map { settings -> settings[LibraryTab.All.id] ?: defaultSortSettings }
            .distinctUntilChanged()
            .flatMapLatest { sort ->
                dependencies.getSortedAllUserBooksUseCase(
                    mode = sort.mode,
                    direction = sort.direction,
                )
            }

        combine(
            sortedBooksFlow,
            dependencies.getAllUserListsUseCase(),
            dependencies.getEnabledStatusCodesAsFlowUseCase(),
            dependencies.getEnabledListIdsAsFlowUseCase(),
        ) { books: List<Book>, lists: List<BookList>, enabledStatuses: Set<Int>, enabledListIds: Set<Int> ->
            val activeStatuses = UserBookStatus.activeLibraryCodes(enabledCodes = enabledStatuses)

            val bookIdsInEnabledLists = lists
                .filter { it.id in enabledListIds }
                .flatMap { list -> list.books.map { it.bookId } }
                .toSet()

            books.filter { book ->
                val statusCode = book.userBook?.status?.code
                (statusCode != null && statusCode in activeStatuses) || book.id in bookIdsInEnabledLists
            }
        }.collectLatest { filtered ->
            scope.setState { state ->
                state.copy(
                    booksByTab = state.booksByTab + (LibraryTab.All.id to filtered),
                    isLoading = false,
                )
            }
        }
    }

    private companion object {
        val defaultSortSettings = LibrarySortSettings.defaultFor(
            mode = LibraryTab.defaultSortMode(tabId = LibraryTab.All.id),
        )
    }
}
