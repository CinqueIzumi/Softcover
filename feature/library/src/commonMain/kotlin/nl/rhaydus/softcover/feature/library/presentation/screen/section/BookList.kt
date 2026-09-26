package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import nl.rhaydus.designsystem.component.DesktopContextMenu
import nl.rhaydus.designsystem.component.DesktopContextMenuItem
import nl.rhaydus.designsystem.component.mutationAnimated
import nl.rhaydus.designsystem.component.rememberLazyItemMutationAnimator
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.modifier.platformModifierClick
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab as LibraryContentTab
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkAddToListSheetShownAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkMoveShelfAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveFromLibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnEnterSelectionModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnReorderShelfBooksAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSelectBookRangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnToggleBookSelectionAction
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

@Composable
internal fun BookList(
    tab: LibraryContentTab,
    state: LibraryUiState,
    gridState: LazyGridState,
    onBookClick: (Book) -> Unit,
    runAction: (LibraryAction) -> Unit,
    columnsOverride: GridCells? = null,
) {
    val rawBooks = state.booksByTab[tab.id]

    if (rawBooks == null) return

    if (rawBooks.isEmpty() && state.isLoading.not()) {
        EmptyListScreen(tab = tab)

        return
    }

    // Books arrive pre-sorted from the DAO via SQL ORDER BY; DisplayListsCollector applies the
    // search + Read-tab year filter off the main thread. Fall back to the raw (already sorted)
    // list for the one frame between the raw books landing and the collector producing the
    // filtered list, so the grid never flashes empty on first load.
    val visibleBooks = state.displayBooksFor(tabId = tab.id) ?: rawBooks

    val visibleBookIds = remember(visibleBooks) { visibleBooks.map { it.id } }

    val sortMode = state.sortModeFor(tabId = tab.id)
    val selectionMode = state.selectionMode

    // Desktop modifier-click anchor: the last cover the user plainly/Ctrl-selected, so a subsequent
    // Shift-click can range-select the visible span up to it. Reset per tab. Inert on touch (no
    // modifier-click fires there).
    var selectionAnchorId by remember(tab.id) { mutableStateOf<Int?>(null) }

    // MANUAL sort renders display-only unless the user has explicitly entered rearrange mode. The
    // saved order stays visible with normal tap/long-press and no handles, so scrolling can't
    // nudge it. The grid is the SAME node either way — items only gain a drag handle and drop
    // their tap targets — so toggling rearrange never disposes and recomposes the grid, which is
    // what flashed every cover (each `AsyncImage` remounting and reloading) on the old
    // swap-between-two-grids path.
    val reorderStatus: UserBookStatus? = if (
        sortMode == LibrarySortMode.MANUAL &&
        tab is LibraryContentTab.Status &&
        tab.status != UserBookStatus.DID_NOT_FINISH &&
        selectionMode.not() &&
        state.isRearranging
    ) {
        tab.status
    } else {
        null
    }

    val isRearranging = reorderStatus != null

    val animator = rememberLazyItemMutationAnimator(keys = visibleBookIds)

    val entry = rememberStaggeredEntryCoordinator(key = "library:books:${tab.id}")

    ScrollToTopOnVisibleSetChange(
        tabId = tab.id,
        sortMode = sortMode,
        sortDirection = state.sortDirectionFor(tabId = tab.id),
        filters = state.filtersFor(tabId = tab.id),
        visibleItemsKey = visibleBookIds.firstOrNull() ?: 0,
        gridState = gridState,
    )

    val haptics = LocalHaptics.current

    val booksById = remember(visibleBooks) { visibleBooks.associateBy { it.id } }

    // Live shadow the reorder library mutates during a drag. Eagerly seeded and kept in
    // lock-step with the canonical (DB-sorted) list so the first frame is already correct and
    // toggling rearrange never blanks the grid; it re-syncs whenever the canonical list changes
    // (e.g. a book shelved or unshelved from elsewhere).
    val orderedIds = remember { visibleBookIds.toMutableStateList() }

    LaunchedEffect(visibleBookIds) {
        if (orderedIds.toList() != visibleBookIds) {
            orderedIds.clear()
            orderedIds.addAll(visibleBookIds)
        }
    }

    // Highest visual index touched during the current drag — defines the prefix the user is
    // re-arranging. Books beyond this index are NOT persisted, so a shallow drag at the top of
    // the shelf leaves the rest in its natural order (and newcomers from the API still slot in
    // just below the prefix).
    val maxTouchedIndex = remember { mutableIntStateOf(-1) }

    // Built for every tab so the grid node is stable across the rearrange toggle. The reorder
    // callback only fires while a handle is attached (rearrange mode), so on the All tab and
    // non-MANUAL sorts it is inert.
    val reorderableState = rememberReorderableLazyGridState(lazyGridState = gridState) { from, to ->
        val fromIndex = orderedIds.indexOf(from.key as Int)
        val toIndex = orderedIds.indexOf(to.key as Int)

        if (fromIndex == -1 || toIndex == -1) return@rememberReorderableLazyGridState

        orderedIds.add(
            toIndex,
            orderedIds.removeAt(fromIndex),
        )

        maxTouchedIndex.intValue = maxOf(
            maxTouchedIndex.intValue,
            fromIndex,
            toIndex,
        )
    }

    // Display mode renders the canonical list directly (byte-identical to before); only while
    // rearranging do we render the live shadow the drag mutates. At the toggle the two are equal
    // (same ids, same order), so swapping the source keeps every key stable — the grid node and
    // every cover persist, no remount.
    val renderIds = if (isRearranging) orderedIds else visibleBookIds

    LayoutGrid(
        layout = state.gridLayout,
        gridState = gridState,
        columnsOverride = columnsOverride,
    ) {
        itemsIndexed(
            renderIds,
            key = { _, id -> id },
            contentType = { _, _ -> "book" },
        ) { index, id ->
            val gridItemScope = this

            val book = booksById[id] ?: return@itemsIndexed
            val cover = state.bookCovers[id]

            ReorderableItem(state = reorderableState, key = id) {
                // Built unconditionally so the per-item composable structure is identical in and
                // out of rearrange mode — only the dragHandle slot and click handlers are
                // swapped below (parameter values, not composable calls), so the cover is never
                // remounted (no flash).
                val handleModifier = Modifier.draggableHandle(
                    onDragStarted = {
                        maxTouchedIndex.intValue = -1

                        haptics.lift()
                    },
                    onDragStopped = {
                        haptics.drop()

                        val touchedDepth = maxTouchedIndex.intValue

                        if (reorderStatus != null && touchedDepth >= 0 && touchedDepth < orderedIds.size) {
                            runAction(
                                OnReorderShelfBooksAction(
                                    status = reorderStatus,
                                    prefixOrderedBookIds = orderedIds
                                        .take(touchedDepth + 1),
                                ),
                            )
                        }
                    },
                )

                val isSelected = selectionMode && book.id in state.selectedBookIds

                // Rearrange mode is drag-only (tap and long-press suppressed); otherwise selection
                // mode toggles, and the default opens the book / long-press enters bulk-select.
                val onClick: () -> Unit = if (isRearranging) {
                    {}
                } else if (selectionMode) {
                    {
                        haptics.select()

                        runAction(OnToggleBookSelectionAction(bookId = book.id))
                    }
                } else {
                    { onBookClick(book) }
                }

                val onLongClick: (() -> Unit)? = if (isRearranging || selectionMode) {
                    null
                } else {
                    {
                        haptics.threshold()

                        runAction(OnEnterSelectionModeAction(bookId = book.id))
                    }
                }

                // Desktop selection affordances, inert on touch: Ctrl/Cmd-click toggles this cover's
                // selection (entering selection mode if needed), Shift-click range-selects the
                // visible span up to the anchor. Suppressed in rearrange mode (drag-only).
                val onCtrlClick = {
                    selectionAnchorId = book.id

                    if (selectionMode) {
                        runAction(OnToggleBookSelectionAction(bookId = book.id))
                    } else {
                        runAction(OnEnterSelectionModeAction(bookId = book.id))
                    }
                }

                val onShiftClick = {
                    val anchor = selectionAnchorId

                    if (anchor == null) {
                        selectionAnchorId = book.id

                        runAction(OnEnterSelectionModeAction(bookId = book.id))
                    } else {
                        runAction(
                            OnSelectBookRangeAction(
                                bookIds = idRangeBetween(
                                    ids = renderIds,
                                    anchorId = anchor,
                                    targetId = book.id,
                                ),
                            ),
                        )
                    }
                }

                // Desktop right-click menu (empty in rearrange mode → pass-through, and a no-op on
                // touch where selection is reached via long-press instead).
                val contextMenuItems = if (isRearranging) {
                    emptyList()
                } else {
                    libraryBookContextMenu(
                        book = book,
                        selectionMode = selectionMode,
                        isSelected = isSelected,
                        onOpen = { onBookClick(book) },
                        onSelect = onCtrlClick,
                        onToggleSelection = {
                            runAction(OnToggleBookSelectionAction(bookId = book.id))
                        },
                        onMarkAsRead = {
                            // In selection mode the menu acts on the whole selection (null →
                            // current selection); otherwise on just the right-clicked book.
                            val targetIds = if (selectionMode) null else setOf(book.id)

                            runAction(
                                OnBulkMoveShelfAction(
                                    status = UserBookStatus.READ,
                                    explicitBookIds = targetIds,
                                ),
                            )
                        },
                        onAddToList = {
                            // Enter-selection commits its setState before the sheet-shown action
                            // runs (TOAD dispatches actions FIFO on one scope and enter-selection
                            // has no suspension point before its setState), so the sheet opens with
                            // this book already selected.
                            if (selectionMode.not()) {
                                runAction(OnEnterSelectionModeAction(bookId = book.id))
                            }

                            runAction(OnBulkAddToListSheetShownAction(shown = true))
                        },
                        onRemove = {
                            val targetIds = if (selectionMode) null else setOf(book.id)

                            runAction(OnBulkRemoveFromLibraryAction(explicitBookIds = targetIds))
                        },
                    )
                }

                val baseModifier = Modifier.mutationAnimated(
                    scope = gridItemScope,
                    animator = animator,
                    itemKey = book.id,
                )
                    .staggeredEntry(
                        coordinator = entry,
                        index = index,
                    )

                val entryModifier = if (isRearranging) {
                    baseModifier
                } else {
                    baseModifier.platformModifierClick(
                        onCtrlClick = onCtrlClick,
                        onShiftClick = onShiftClick,
                    )
                }

                DesktopContextMenu(items = contextMenuItems) {
                    LayoutBookEntry(
                        modifier = entryModifier,
                        book = book,
                        cover = cover,
                        layout = state.gridLayout,
                        onClick = onClick,
                        onLongClick = onLongClick,
                        isSelectionMode = selectionMode,
                        isSelected = isSelected,
                        deadlineProgress = state.deadlineProgressByBook[book.id],
                        deadlineBadge = state.deadlineBadges[book.id],
                        deadlineSummary = state.deadlineSummaries[book.id],
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
}

/**
 * The desktop right-click menu for a book cover/row. Offers Open + the selection entry point
 * (Select, or Add/Remove-from-selection while a selection is active) plus the per-book operations
 * that reuse the bulk pipeline on a single book (Mark as read — hidden when already Read — Add to
 * list, Remove). Empty on touch (the caller passes an empty list outside desktop / in rearrange mode).
 */
private fun libraryBookContextMenu(
    book: Book,
    selectionMode: Boolean,
    isSelected: Boolean,
    onOpen: () -> Unit,
    onSelect: () -> Unit,
    onToggleSelection: () -> Unit,
    onMarkAsRead: () -> Unit,
    onAddToList: () -> Unit,
    onRemove: () -> Unit,
): List<DesktopContextMenuItem> = buildList {
    add(DesktopContextMenuItem(
        label = "Open",
        onClick = onOpen,
    ),)

    if (selectionMode) {
        val toggleLabel = if (isSelected) "Remove from selection" else "Add to selection"

        add(DesktopContextMenuItem(
            label = toggleLabel,
            onClick = onToggleSelection,
        ),)
    } else {
        add(DesktopContextMenuItem(
            label = "Select",
            onClick = onSelect,
        ),)
    }

    if (book.status != BookStatus.Read) {
        add(DesktopContextMenuItem(
            label = "Mark as read",
            onClick = onMarkAsRead,
        ),)
    }

    add(DesktopContextMenuItem(
        label = "Add to list…",
        onClick = onAddToList,
    ),)
    add(DesktopContextMenuItem(
        label = "Remove from library",
        onClick = onRemove,
    ),)
}

/**
 * The visible span of book ids between the selection [anchorId] and the shift-clicked [targetId]
 * (inclusive), in display order. Falls back to just the target when either id is no longer visible
 * (e.g. filtered out between clicks).
 */
private fun idRangeBetween(
    ids: List<Int>,
    anchorId: Int,
    targetId: Int,
): List<Int> {
    val anchorIndex = ids.indexOf(anchorId)
    val targetIndex = ids.indexOf(targetId)

    if (anchorIndex == -1 || targetIndex == -1) return listOf(targetId)

    return ids.subList(
        minOf(
            anchorIndex,
            targetIndex,
        ),
        maxOf(
            anchorIndex,
            targetIndex,
        ) + 1,
    ).toList()
}
