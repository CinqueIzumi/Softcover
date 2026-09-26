package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import nl.rhaydus.designsystem.component.mutationAnimated
import nl.rhaydus.designsystem.component.rememberLazyItemMutationAnimator
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.presentation.model.LibraryTab as LibraryContentTab
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnReorderListBooksAction
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

@Composable
internal fun EditionList(
    tab: LibraryContentTab.CustomList,
    state: LibraryUiState,
    gridState: LazyGridState,
    onEditionClick: (BookEdition) -> Unit,
    runAction: (LibraryAction) -> Unit,
    columnsOverride: GridCells? = null,
) {
    val rawEditions = state.editionsByTab[tab.id] ?: return

    if (rawEditions.isEmpty() && state.isLoading.not()) {
        EmptyListScreen(tab = tab)

        return
    }

    // Custom-list editions still sort in memory — the dataset is small (dozens, not
    // thousands) so the sort is cheap and the SQL-sort refactor is books-only. Fall back to the
    // raw (already source-ordered) list for the one frame between the raw editions landing and
    // DisplayListsCollector producing the searched/sorted list, so the grid never flashes empty.
    val visibleEditions = state.displayEditionsFor(tabId = tab.id) ?: rawEditions

    val visibleEditionIds = remember(visibleEditions) { visibleEditions.map { it.id } }

    val sortMode = state.sortModeFor(tabId = tab.id)

    val isRanked = state.customLists.firstOrNull { it.id == tab.listId }?.ranked == true

    // ORDER sort renders display-only until the user enters rearrange mode. The grid is the
    // SAME node either way — items only gain a drag handle and drop their tap target — so
    // toggling rearrange never disposes and recomposes the grid, which is what flashed every
    // cover (each `AsyncImage` remounting and reloading) on the old swap-between-two-grids path.
    val isRearranging = sortMode == LibrarySortMode.ORDER && isRanked && state.isRearranging

    val animator = rememberLazyItemMutationAnimator(keys = visibleEditionIds)

    val entry = rememberStaggeredEntryCoordinator(key = "library:editions:${tab.id}")

    ScrollToTopOnVisibleSetChange(
        tabId = tab.id,
        sortMode = sortMode,
        sortDirection = state.sortDirectionFor(tabId = tab.id),
        filters = state.filtersFor(tabId = tab.id),
        visibleItemsKey = visibleEditionIds.firstOrNull() ?: 0,
        gridState = gridState,
    )

    val haptics = LocalHaptics.current

    // Live shadow the reorder library mutates during a drag. Eagerly seeded and kept in
    // lock-step with the canonical list so the first frame is already correct — an empty seed
    // synced in only via the LaunchedEffect would blank the grid for a frame.
    val orderedIds = remember { visibleEditionIds.toMutableStateList() }

    LaunchedEffect(visibleEditionIds) {
        if (orderedIds.toList() != visibleEditionIds) {
            orderedIds.clear()
            orderedIds.addAll(visibleEditionIds)
        }
    }

    val editionsById = remember(visibleEditions) { visibleEditions.associateBy { it.id } }

    // Lookup from editionId → listBookId, drawn from the canonical `customLists` snapshot
    // for this tab. `editionsByTab` only carries `BookEdition`s, so without this map we'd
    // have no way to identify which `list_books` row each card represents.
    val listBookIdByEditionId: Map<Int, Int> = remember(
        state.customLists,
        tab.listId,
    ) {
        state.customLists.firstOrNull { it.id == tab.listId }
            ?.books
            ?.associate { it.editionId to it.listBookId }
            .orEmpty()
    }

    val minTouchedIndex = remember { mutableIntStateOf(-1) }
    val maxTouchedIndex = remember { mutableIntStateOf(-1) }

    // Built for every list so the grid node is stable across the rearrange toggle. The reorder
    // callback only fires while a handle is attached (rearrange mode), so for unranked or
    // unordered lists it is inert. Persistence is range-scoped: unlike the built-in shelf path
    // (which writes a prefix `0..maxTouched`), Hardcover's web client rewrites only the
    // contiguous `[minTouched, maxTouched]` range, and we mirror that so two clients editing the
    // same list stay consistent.
    val reorderableState = rememberReorderableLazyGridState(lazyGridState = gridState) { from, to ->
        val fromIndex = orderedIds.indexOf(from.key as Int)
        val toIndex = orderedIds.indexOf(to.key as Int)

        if (fromIndex == -1 || toIndex == -1) return@rememberReorderableLazyGridState

        orderedIds.add(
            toIndex,
            orderedIds.removeAt(fromIndex),
        )

        val current = minTouchedIndex.intValue

        minTouchedIndex.intValue = if (current == -1) {
            minOf(
                fromIndex,
                toIndex,
            )
        } else {
            minOf(
                current,
                fromIndex,
                toIndex,
            )
        }

        maxTouchedIndex.intValue = maxOf(
            maxTouchedIndex.intValue,
            fromIndex,
            toIndex,
        )
    }

    // Display mode renders the canonical list directly; only while rearranging do we render the
    // live shadow the drag mutates. At the toggle the two are equal, so the source swap keeps
    // every key stable — the grid node and every cover persist, no remount (no flash).
    val renderIds = if (isRearranging) orderedIds else visibleEditionIds

    LayoutGrid(
        layout = state.gridLayout,
        gridState = gridState,
        columnsOverride = columnsOverride,
    ) {
        itemsIndexed(
            renderIds,
            key = { _, id -> id },
            contentType = { _, _ -> "edition" },
        ) { index, id ->
            val gridItemScope = this

            val edition = editionsById[id] ?: return@itemsIndexed
            val cover = state.editionCovers[id]

            ReorderableItem(state = reorderableState, key = id) {
                // Built unconditionally so the per-item composable structure is identical whether
                // or not rearranging — only the dragHandle slot and tap target are toggled below,
                // so the cover is never remounted (no flash).
                val handleModifier = Modifier.draggableHandle(
                    onDragStarted = {
                        minTouchedIndex.intValue = -1
                        maxTouchedIndex.intValue = -1

                        haptics.lift()
                    },
                    onDragStopped = {
                        haptics.drop()

                        val min = minTouchedIndex.intValue
                        val max = maxTouchedIndex.intValue

                        if (isRearranging.not() || min < 0 || max < 0 || max >= orderedIds.size) {
                            return@draggableHandle
                        }

                        val orderedListBookIds = orderedIds
                            .subList(
                                min,
                                max + 1,
                            )
                            .mapNotNull { editionId -> listBookIdByEditionId[editionId] }

                        if (orderedListBookIds.size != max - min + 1) {
                            // A list_books row was missing for one of the dragged
                            // editions — bail rather than write a partial range.
                            return@draggableHandle
                        }

                        runAction(
                            OnReorderListBooksAction(
                                listId = tab.listId,
                                startPosition = min,
                                orderedListBookIds = orderedListBookIds,
                            ),
                        )
                    },
                )

                // Drag-only while rearranging: tapping a cover doesn't open the edition with the
                // handle live (matches the built-in shelf grid).
                LayoutEditionEntry(
                    modifier = Modifier.mutationAnimated(
                        scope = gridItemScope,
                        animator = animator,
                        itemKey = edition.id,
                    )
                        .staggeredEntry(
                            coordinator = entry,
                            index = index,
                        ),
                    edition = edition,
                    cover = cover,
                    layout = state.gridLayout,
                    onEditionClick = if (isRearranging) {
                        {}
                    } else {
                        onEditionClick
                    },
                    dragHandle = if (isRearranging) {
                        { DragHandle(modifier = handleModifier) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}
