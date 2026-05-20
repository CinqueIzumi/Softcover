package nl.rhaydus.softcover.feature.library.presentation.flows

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortSettings

@OptIn(ExperimentalCoroutinesApi::class)
class BooksByStatusCollector : LibraryInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        // Each status tab has its own persisted sort. The DAO emits an already-sorted list per
        // status; flatMapLatest re-subscribes whenever the user changes that tab's sort settings.
        combine(
            dependencies.getEnabledStatusCodesAsFlowUseCase(),
            booksForStatus(dependencies = dependencies, status = UserBookStatus.CURRENTLY_READING),
            booksForStatus(dependencies = dependencies, status = UserBookStatus.WANT_TO_READ),
            booksForStatus(dependencies = dependencies, status = UserBookStatus.READ),
            booksForStatus(dependencies = dependencies, status = UserBookStatus.DID_NOT_FINISH),
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

    private fun booksForStatus(
        dependencies: LibraryDependencies,
        status: UserBookStatus,
    ): Flow<List<Book>> {
        val tabId = LibraryTab.Status.of(status).id

        val defaultSortSettings = LibrarySortSettings.defaultFor(
            mode = LibraryTab.defaultSortMode(tabId = tabId),
        )

        return dependencies.getLibrarySortSettingsAsFlowUseCase()
            .map { settings -> settings[tabId] ?: defaultSortSettings }
            .distinctUntilChanged()
            .flatMapLatest { sort ->
                dependencies.getSortedBooksByStatusUseCase(
                    status = status,
                    mode = sort.mode,
                    direction = sort.direction,
                )
            }
    }
}
