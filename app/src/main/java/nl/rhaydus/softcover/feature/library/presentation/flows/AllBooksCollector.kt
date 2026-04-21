package nl.rhaydus.softcover.feature.library.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

class AllBooksCollector : LibraryInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        combine(
            dependencies.getAllUserBooksUseCase(),
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
}
