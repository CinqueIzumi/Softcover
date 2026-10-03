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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import nl.rhaydus.designsystem.component.mutationAnimated
import nl.rhaydus.designsystem.component.rememberLazyItemMutationAnimator
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.bookcard.rememberBookCardEventHandler
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.model.LibraryTab as LibraryContentTab
import nl.rhaydus.softcover.core.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnReorderListBooksAction
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.isEditionListRearranging

@Composable
internal fun EditionList(
    tab: LibraryContentTab.CustomList,
    state: LibraryUiState,
    gridState: LazyGridState,
    onEditionClick: (BookEdition) -> Unit,
    runAction: (LibraryAction) -> Unit,
    columnsOverride: GridCells? = null,
) {
    val rawEditions = state.editionsByTab[tab.id]

    if (rawEditions == null || (rawEditions.isEmpty() && state.isLoading)) {
        LibraryLoadingGrid(
            layout = state.gridLayout,
            gridState = gridState,
            columnsOverride = columnsOverride,
        )

        return
    }

    if (rawEditions.isEmpty()) {
        state.emptyStatesByTab[tab.id]?.let { emptyState ->
            EmptyState(
                model = emptyState,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
                    .padding(top = 40.dp),
            )
        }

        return
    }

    val tabCards = state.editionCardsByTab[tab.id]

    if (tabCards == null) {
        LibraryLoadingGrid(
            layout = state.gridLayout,
            gridState = gridState,
            columnsOverride = columnsOverride,
        )

        return
    }

    val cards = tabCards.cards

    // Custom-list editions still sort in memory — the dataset is small (dozens, not
    // thousands) so the sort is cheap and the SQL-sort refactor is books-only.
    // BookCardModelsCollector builds the cards from the searched/sorted list. The cards carry the
    // order and their own editions, so a card's event resolves to the edition it was built from
    // even while the live lists are ahead of the cards.
    val editionsByKey = tabCards.payloads

    val visibleKeys = remember(cards) { cards.map { it.key.lazyKey } }

    val editionIdByLazyKey = remember(cards) {
        cards.mapNotNull { card ->
            (card.key as? BookCardKey.Edition)?.let { card.key.lazyKey to it.editionId }
        }.toMap()
    }

    val sortMode = state.sortModeFor(tabId = tab.id)

    val isRanked = state.customLists.firstOrNull { it.id == tab.listId }?.ranked == true

    // ORDER sort renders display-only until the user enters rearrange mode. The grid is the
    // SAME node either way — items only gain a drag handle and drop their tap target — so
    // toggling rearrange never disposes and recomposes the grid, which is what flashed every
    // cover (each `AsyncImage` remounting and reloading) on the old swap-between-two-grids path.
    val isRearranging = isEditionListRearranging(
        sortMode = sortMode,
        isRanked = isRanked,
        isRearranging = state.isRearranging,
    )

    val animator = rememberLazyItemMutationAnimator(keys = visibleKeys)

    val entry = rememberStaggeredEntryCoordinator(key = "library:editions:${tab.id}")

    ScrollToTopOnVisibleSetChange(
        tabId = tab.id,
        sortMode = sortMode,
        sortDirection = state.sortDirectionFor(tabId = tab.id),
        filters = state.filtersFor(tabId = tab.id),
        visibleItemsKey = visibleKeys.firstOrNull()?.let(editionIdByLazyKey::get) ?: 0,
        gridState = gridState,
    )

    val haptics = LocalHaptics.current

    // Live shadow the reorder library mutates during a drag. Eagerly seeded and kept in
    // lock-step with the canonical list so the first frame is already correct — an empty seed
    // synced in only via the LaunchedEffect would blank the grid for a frame.
    val orderedKeys = remember { visibleKeys.toMutableStateList() }

    LaunchedEffect(visibleKeys) {
        if (orderedKeys.toList() != visibleKeys) {
            orderedKeys.clear()
            orderedKeys.addAll(visibleKeys)
        }
    }

    val prefetcher = LocalBookDetailPrefetcher.current

    // One handler for the whole grid; the cards route their events here by key.
    val onCardEvent = rememberBookCardEventHandler(
        resolve = editionsByKey::get,
    ) { event, edition ->
        when (event) {
            is BookCardEvent.Clicked -> onEditionClick(edition)

            is BookCardEvent.PressStarted -> prefetcher?.prefetch(edition.bookId)

            is BookCardEvent.LongClicked,
            is BookCardEvent.TrailingClicked,
            is BookCardEvent.MenuClicked,
            is BookCardEvent.ShakeEnded,
                -> Unit
        }
    }

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
        val fromIndex = orderedKeys.indexOf(from.key as String)
        val toIndex = orderedKeys.indexOf(to.key as String)

        if (fromIndex == -1 || toIndex == -1) return@rememberReorderableLazyGridState

        orderedKeys.add(
            toIndex,
            orderedKeys.removeAt(fromIndex),
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

    // Display mode renders the canonical cards directly; only while rearranging do we render the
    // live shadow the drag mutates. At the toggle the two are equal, so the source swap keeps
    // every key stable — the grid node and every cover persist, no remount (no flash).
    val cardsByLazyKey = remember(cards) { cards.associateBy { it.key.lazyKey } }

    val renderCards = if (isRearranging) orderedKeys.mapNotNull { cardsByLazyKey[it] } else cards

    LayoutGrid(
        layout = tabCards.layout,
        gridState = gridState,
        columnsOverride = columnsOverride,
    ) {
        itemsIndexed(
            renderCards,
            key = { _, card -> card.key.lazyKey },
            contentType = { _, _ -> "edition" },
        ) { index, card ->
            val gridItemScope = this

            ReorderableItem(state = reorderableState, key = card.key.lazyKey) {
                // Built unconditionally so the per-item composable structure is identical whether
                // or not rearranging — only the drag-handle slot is toggled below, so the cover is
                // never remounted (no flash).
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

                        if (isRearranging.not() || min < 0 || max < 0 || max >= orderedKeys.size) {
                            return@draggableHandle
                        }

                        val orderedListBookIds = orderedKeys
                            .subList(
                                min,
                                max + 1,
                            )
                            .mapNotNull { key -> editionIdByLazyKey[key]?.let(listBookIdByEditionId::get) }

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

                val entryModifier = Modifier.mutationAnimated(
                    scope = gridItemScope,
                    animator = animator,
                    itemKey = card.key.lazyKey,
                )
                    .staggeredEntry(
                        coordinator = entry,
                        index = index,
                    )

                BookCard(
                    model = card,
                    onEvent = onCardEvent,
                    modifier = entryModifier,
                    content = if (isRearranging) {
                        { DragHandle(modifier = handleModifier) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}
