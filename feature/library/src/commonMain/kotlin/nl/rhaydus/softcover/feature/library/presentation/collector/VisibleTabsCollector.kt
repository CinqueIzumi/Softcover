package nl.rhaydus.softcover.feature.library.presentation.collector

import androidx.compose.foundation.lazy.grid.LazyGridState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

private val statusOrder: List<UserBookStatus> = listOf(
    UserBookStatus.CURRENTLY_READING,
    UserBookStatus.WANT_TO_READ,
    UserBookStatus.READ,
    UserBookStatus.DID_NOT_FINISH,
)

internal class VisibleTabsCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        combine(
            dependencies.getEnabledStatusCodesAsFlowUseCase(),
            dependencies.getEnabledListIdsAsFlowUseCase(),
            dependencies.getAllUserListsUseCase(),
            dependencies.getLibraryTabOrderAsFlowUseCase(),
        ) { enabledStatuses: Set<Int>, enabledListIds: Set<Int>, lists: List<BookList>, persistedOrder: List<String> ->
            val active = UserBookStatus.activeLibraryCodes(enabledCodes = enabledStatuses)

            val statusTabs = statusOrder
                .filter { it.code in active }
                .map { LibraryTab.Status.of(it) }

            val listTabs = lists
                .filter { it.id in enabledListIds }
                .sortedBy { it.name.lowercase() }
                .map { LibraryTab.CustomList(
                    listId = it.id,
                    listName = it.name,
                ) }

            val defaultOrdered = buildList<LibraryTab> {
                add(LibraryTab.All)
                addAll(statusTabs)
                addAll(listTabs)
            }

            applyTabOrder(
                tabs = defaultOrdered,
                persistedOrder = persistedOrder,
            )
        }.collectLatest { tabs ->
            val tabIds = tabs.map { it.id }.toSet()

            scope.setState { state ->
                val stillVisible = state.selectedTabId in tabIds
                val fallback = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id

                state.copy(
                    visibleTabs = tabs,
                    tabsLoaded = true,
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

private fun applyTabOrder(
    tabs: List<LibraryTab>,
    persistedOrder: List<String>,
): List<LibraryTab> {
    if (persistedOrder.isEmpty()) return tabs

    val pinned = tabs.firstOrNull { it is LibraryTab.All }
    val reorderable = tabs.filter { it !is LibraryTab.All }

    val byId = reorderable.associateBy { it.id }

    val ordered = persistedOrder.mapNotNull { byId[it] }

    val orderedIds = ordered.map { it.id }.toSet()

    val appended = reorderable.filter { it.id !in orderedIds }

    return listOfNotNull(pinned) + ordered + appended
}
