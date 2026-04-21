package nl.rhaydus.softcover.feature.library.presentation.flows

import androidx.compose.foundation.lazy.grid.LazyGridState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

private val statusOrder: List<UserBookStatus> = listOf(
    UserBookStatus.CURRENTLY_READING,
    UserBookStatus.WANT_TO_READ,
    UserBookStatus.READ,
    UserBookStatus.DID_NOT_FINISH,
)

class VisibleTabsCollector : LibraryInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        combine(
            dependencies.getEnabledStatusCodesAsFlowUseCase(),
            dependencies.getEnabledListIdsAsFlowUseCase(),
            dependencies.getAllUserListsUseCase(),
        ) { enabledStatuses: Set<Int>, enabledListIds: Set<Int>, lists: List<BookList> ->
            val active = UserBookStatus.activeLibraryCodes(enabledCodes = enabledStatuses)

            val statusTabs = statusOrder
                .filter { it.code in active }
                .map { LibraryTab.Status.of(it) }

            val listTabs = lists
                .filter { it.id in enabledListIds }
                .sortedBy { it.name.lowercase() }
                .map { LibraryTab.CustomList(listId = it.id, listName = it.name) }

            buildList<LibraryTab> {
                add(LibraryTab.All)
                addAll(statusTabs)
                addAll(listTabs)
            }
        }.collectLatest { tabs ->
            val tabIds = tabs.map { it.id }.toSet()

            scope.setState { state ->
                val stillVisible = state.selectedTabId in tabIds
                val fallback = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id

                state.copy(
                    visibleTabs = tabs,
                    selectedTabId = if (stillVisible) state.selectedTabId else fallback,
                )
            }

            scope.setLocalVariables { locals ->
                val kept: Map<String, LazyGridState> = locals.gridStates.filterKeys { it in tabIds }
                val additions: Map<String, LazyGridState> = tabIds
                    .filterNot { it in kept }
                    .associateWith { LazyGridState() }

                locals.copy(gridStates = kept + additions)
            }
        }
    }
}
