package nl.rhaydus.softcover.feature.library.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

class BooksByStatusCollector : LibraryInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        combine(
            dependencies.getAllUserBooksUseCase(),
            dependencies.getEnabledStatusCodesAsFlowUseCase(),
        ) { books: List<Book>, enabledStatuses: Set<Int> ->
            UserBookStatus.activeLibraryCodes(enabledCodes = enabledStatuses)
                .mapNotNull { code -> UserBookStatus.entries.firstOrNull { it.code == code } }
                .filter { it.isExposedInLibrary }
                .associate { status ->
                    val tabId = LibraryTab.Status.of(status).id
                    tabId to books.filter { it.userBook?.status?.code == status.code }
                }
        }.collectLatest { grouped ->
            scope.setState { state ->
                val retainedStatusKeys = grouped.keys
                val stripped = state.booksByTab.filterKeys { key ->
                    key.startsWith("status-").not() || key in retainedStatusKeys
                }
                state.copy(booksByTab = stripped + grouped)
            }
        }
    }
}
