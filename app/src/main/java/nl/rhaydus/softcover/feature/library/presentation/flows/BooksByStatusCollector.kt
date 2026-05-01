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
            dependencies.getEnabledStatusCodesAsFlowUseCase(),
            dependencies.getCurrentlyReadingUserBooksUseCase(),
            dependencies.getWantToReadUserBooksUseCase(),
            dependencies.getReadUserBooksUseCase(),
            dependencies.getDidNotFinishUserBooksUseCase(),
        ) { enabledStatuses: Set<Int>, currentlyReading: List<Book>, wantToRead: List<Book>, read: List<Book>, didNotFinish: List<Book> ->
            val activeCodes = UserBookStatus.activeLibraryCodes(enabledCodes = enabledStatuses)

            val booksByStatus: Map<UserBookStatus, List<Book>> = mapOf(
                UserBookStatus.CURRENTLY_READING to currentlyReading,
                UserBookStatus.WANT_TO_READ to wantToRead,
                UserBookStatus.READ to read,
                UserBookStatus.DID_NOT_FINISH to didNotFinish,
            )

            booksByStatus
                .filterKeys { it.code in activeCodes }
                .mapKeys { (status, _) -> LibraryTab.Status.of(status).id }
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
