package nl.rhaydus.softcover.feature.library.presentation.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleCombinedClickable
import nl.rhaydus.softcover.core.designsystem.presentation.component.DeadlineBadge
import nl.rhaydus.softcover.core.designsystem.presentation.component.DeadlineCoverOverlay
import nl.rhaydus.softcover.core.designsystem.presentation.component.DeadlineSummaryLine
import nl.rhaydus.softcover.core.designsystem.presentation.component.DesktopContextMenu
import nl.rhaydus.softcover.core.designsystem.presentation.component.DesktopContextMenuItem
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.designsystem.presentation.component.mutationAnimated
import nl.rhaydus.softcover.core.designsystem.presentation.component.rememberLazyItemMutationAnimator
import nl.rhaydus.softcover.core.designsystem.presentation.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.softcover.core.designsystem.presentation.component.staggeredEntry
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.layout.WindowWidthClass
import nl.rhaydus.softcover.core.designsystem.presentation.layout.rememberWindowSizeClass
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab as LibraryContentTab
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.platformModifierClick
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.prefetchBookDetailOnPress
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.designsystem.presentation.util.rememberBottomBarPadding
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkAddToListSheetShownAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkMoveShelfAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveFromLibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnEnterSelectionModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnReorderListBooksAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnReorderShelfBooksAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSelectBookRangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnToggleBookSelectionAction
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.util.formatBookCount
import nl.rhaydus.softcover.feature.library.presentation.util.formatPageCount

// region Shelf content
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
    // thousands) so the sort is cheap and the SQL-sort refactor is books-only.
    val visibleEditions = state.displayEditionsFor(tabId = tab.id).orEmpty()

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
        itemsIndexed(renderIds, key = { _, id -> id }) { index, id ->
            val gridItemScope = this

            val edition = editionsById[id] ?: return@itemsIndexed

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

    // Books arrive pre-sorted from the DAO via SQL ORDER BY. The displayBooksFor call here
    // only applies the in-memory search + Read-tab year filter, which is cheap (one pass
    // over the list, no allocation when no filter is active).
    val visibleBooks = state.displayBooksFor(tabId = tab.id).orEmpty()

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
        itemsIndexed(renderIds, key = { _, id -> id }) { index, id ->
            val gridItemScope = this

            val book = booksById[id] ?: return@itemsIndexed

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
                        layout = state.gridLayout,
                        onClick = onClick,
                        onLongClick = onLongClick,
                        isSelectionMode = selectionMode,
                        isSelected = isSelected,
                        deadline = state.deadlines[book.id],
                        dateStyle = state.dateStyle,
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

/**
 * Small grab affordance shown only while a shelf is in MANUAL sort. The icon itself is the
 * drag handle — the caller attaches `Modifier.draggableHandle(...)` from the reorder library's
 * item scope.
 */
@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(percent = 50),
    ) {
        val dragHandleIcon = SoftcoverIconResource.Drawable(
            icon = SoftcoverIcon.DragHandle,
            contentDescription = "Drag to reorder",
        )

        Icon(
            painter = dragHandleIcon.getIconPainter(),
            contentDescription = dragHandleIcon.contentDescription,
            modifier = Modifier
                .size(28.dp)
                .padding(4.dp),
        )
    }
}

@Composable
private fun ScrollToTopOnVisibleSetChange(
    tabId: String,
    sortMode: LibrarySortMode,
    sortDirection: SortDirection,
    filters: LibraryFilters,
    visibleItemsKey: Any,
    gridState: LazyGridState,
) {
    var previousKey by remember(tabId) {
        mutableStateOf<Triple<LibrarySortMode, SortDirection, LibraryFilters>?>(null)
    }
    var pendingScrollToTop by remember(tabId) { mutableStateOf(false) }

    // Sort or filter change just marks intent. We don't scroll here because the visible books
    // haven't updated yet — scrolling now would race with LazyGrid's "follow the focused item
    // by key" behavior once the new visible list lands and silently undo the scroll.
    LaunchedEffect(tabId, sortMode, sortDirection, filters) {
        val current = Triple(
            sortMode,
            sortDirection,
            filters,
        )
        val prior = previousKey

        if (prior != null && prior != current) {
            pendingScrollToTop = true
        }

        previousKey = current
    }

    // After the new sorted list arrives ([visibleItemsKey] flips), perform the actual scroll.
    // Snap (not animated) so it doesn't compete with the per-item placement animation.
    LaunchedEffect(visibleItemsKey) {
        if (pendingScrollToTop) {
            pendingScrollToTop = false

            gridState.scrollToItem(index = 0)
        }
    }
}

@Composable
private fun LayoutGrid(
    layout: LibraryGridLayout,
    gridState: LazyGridState,
    columnsOverride: GridCells? = null,
    content: LazyGridScope.() -> Unit,
) {
    // Mobile maps each layout to a fixed column count; desktop passes [columnsOverride] (a
    // pane-width-adaptive `GridCells`) so a wide window fills with more columns. Item spacing and
    // edge padding stay layout-derived — shared across both platforms.
    val columns: GridCells = columnsOverride ?: GridCells.Fixed(
        count = when (layout) {
            LibraryGridLayout.GRID_TWO_COLUMNS,
            LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
                -> 2

            LibraryGridLayout.GRID_THREE_COLUMNS,
            LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
                -> 3

            LibraryGridLayout.LIST_COMPACT,
            LibraryGridLayout.LIST_LARGE,
                -> 1
        },
    )

    val itemSpacing = when (layout) {
        LibraryGridLayout.GRID_TWO_COLUMNS -> 20.dp
        LibraryGridLayout.GRID_THREE_COLUMNS -> 16.dp
        LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY -> 14.dp
        LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY -> 10.dp
        LibraryGridLayout.LIST_LARGE -> 12.dp
        LibraryGridLayout.LIST_COMPACT -> 0.dp
    }

    val horizontalPadding = when (layout) {
        LibraryGridLayout.LIST_COMPACT -> 24.dp
        else -> 16.dp
    }

    LazyVerticalGrid(
        columns = columns,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        contentPadding = PaddingValues(bottom = rememberBottomBarPadding()),
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        state = gridState,
        content = content,
    )
}

@Composable
private fun LayoutBookEntry(
    book: Book,
    layout: LibraryGridLayout,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    deadline: BookDeadline? = null,
    dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
    dragHandle: (@Composable () -> Unit)? = null,
) {
    // Prefetch only makes sense when the tap opens book detail. In selection mode the tap
    // toggles selection, so skip the prefetch to avoid spending bandwidth on a navigation
    // that won't happen.
    val entryModifier = if (isSelectionMode) modifier else modifier.prefetchBookDetailOnPress(book.id)

    val authorName = book.authors.map { it.name }.firstOrNull().orEmpty()

    val currentEdition = book.currentEdition
    val deadlineProgress = deadline?.let {
        if (currentEdition == null) return@let null
        val current = when (it.unit) {
            DeadlineUnit.PAGES -> book.userBookRead?.currentPage ?: 0
            DeadlineUnit.SECONDS -> book.userBookRead?.currentSeconds ?: 0
        }
        val total = when (it.unit) {
            DeadlineUnit.PAGES -> currentEdition.pages ?: 0
            DeadlineUnit.SECONDS -> currentEdition.audioSeconds ?: 0
        }

        DeadlineProgress.compute(
            deadline = it,
            current = current,
            total = total,
        )
    }

    when (layout) {
        LibraryGridLayout.GRID_TWO_COLUMNS,
        LibraryGridLayout.GRID_THREE_COLUMNS,
            -> {
            CoverGridOverlay(dragHandle = dragHandle) {
                GridBookCell(
                    modifier = entryModifier,
                    title = book.title,
                    authorName = authorName,
                    onClick = onClick,
                    onLongClick = onLongClick,
                ) { coverModifier ->
                    SelectableCover(
                        modifier = coverModifier,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                    ) {
                        DeadlineCoverOverlay(progress = deadlineProgress) {
                            EditionImage(
                                edition = currentEdition,
                                modifier = Modifier.fillMaxSize(),
                                isLoading = false,
                                defaultEdition = book.defaultEdition,
                                fallbackCoverUrl = book.coverUrl,
                                elevation = 6.dp,
                                cornerRadius = 10.dp,
                                sharedTransitionKey = bookCoverTransitionKey(
                                    editionId = currentEdition?.id,
                                    bookId = book.id,
                                ),
                            )
                        }
                    }
                }
            }
        }

        LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
        LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            -> {
            CoverGridOverlay(dragHandle = dragHandle) {
                CoverOnlyCell(
                    modifier = entryModifier,
                    onClick = onClick,
                    onLongClick = onLongClick,
                ) { coverModifier ->
                    SelectableCover(
                        modifier = coverModifier,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                    ) {
                        DeadlineCoverOverlay(progress = deadlineProgress) {
                            EditionImage(
                                edition = currentEdition,
                                modifier = Modifier.fillMaxSize(),
                                isLoading = false,
                                defaultEdition = book.defaultEdition,
                                fallbackCoverUrl = book.coverUrl,
                                coverlessTitle = book.title,
                                elevation = 6.dp,
                                cornerRadius = 10.dp,
                                sharedTransitionKey = bookCoverTransitionKey(
                                    editionId = currentEdition?.id,
                                    bookId = book.id,
                                ),
                            )
                        }
                    }
                }
            }
        }

        LibraryGridLayout.LIST_COMPACT -> {
            CompactRow(
                modifier = entryModifier,
                title = book.title,
                authorName = authorName,
                onClick = onClick,
                onLongClick = onLongClick,
                isSelectionMode = isSelectionMode,
                isSelected = isSelected,
                deadlineProgress = deadlineProgress,
                trailing = dragHandle,
            )
        }

        LibraryGridLayout.LIST_LARGE -> {
            LargeRow(
                modifier = entryModifier,
                title = book.title,
                authorName = currentEdition?.authorString.orEmpty(),
                onClick = onClick,
                onLongClick = onLongClick,
                seriesText = book.seriesText,
                releaseYear = book.releaseYear,
                usersCount = book.usersCount,
                rating = book.rating,
                deadlineProgress = deadlineProgress,
                dateStyle = dateStyle,
                trailing = dragHandle,
            ) { coverModifier ->
                SelectableCover(
                    modifier = coverModifier,
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                ) {
                    DeadlineCoverOverlay(progress = deadlineProgress) {
                        EditionImage(
                            edition = currentEdition,
                            modifier = Modifier.fillMaxSize(),
                            isLoading = false,
                            defaultEdition = book.defaultEdition,
                            fallbackCoverUrl = book.coverUrl,
                            elevation = 6.dp,
                            cornerRadius = 10.dp,
                            sharedTransitionKey = bookCoverTransitionKey(
                                editionId = currentEdition?.id,
                                bookId = book.id,
                            ),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Wraps a grid cell in a Box so a [dragHandle] can be overlaid on the top-right corner of the
 * cover. Used by [LayoutBookEntry] / [LayoutEditionEntry] for the GRID_* layouts where there is
 * no inline trailing slot; the handle sits visually on the cover. When [dragHandle] is null the
 * cell renders without an extra wrapping Box.
 */
@Composable
private fun CoverGridOverlay(
    dragHandle: (@Composable () -> Unit)?,
    cell: @Composable () -> Unit,
) {
    if (dragHandle == null) {
        cell()
    } else {
        Box {
            cell()

            Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                dragHandle()
            }
        }
    }
}

@Composable
private fun LayoutEditionEntry(
    edition: BookEdition,
    layout: LibraryGridLayout,
    onEditionClick: (BookEdition) -> Unit,
    modifier: Modifier = Modifier,
    dragHandle: (@Composable () -> Unit)? = null,
) {
    val entryModifier = modifier.prefetchBookDetailOnPress(edition.bookId)

    val title = edition.title.orEmpty()
    val authorName = edition.authors.map { it.name }.firstOrNull().orEmpty()

    when (layout) {
        LibraryGridLayout.GRID_TWO_COLUMNS,
        LibraryGridLayout.GRID_THREE_COLUMNS,
            -> {
            CoverGridOverlay(dragHandle = dragHandle) {
                GridBookCell(
                    modifier = entryModifier,
                    title = title,
                    authorName = authorName,
                    onClick = { onEditionClick(edition) },
                ) { coverModifier ->
                    EditionImage(
                        edition = edition,
                        modifier = coverModifier,
                        isLoading = false,
                        defaultEdition = edition,
                        elevation = 6.dp,
                        cornerRadius = 10.dp,
                        sharedTransitionKey = bookCoverTransitionKey(
                            editionId = edition.id,
                            bookId = edition.bookId,
                            surface = "edition-${edition.id}",
                        ),
                    )
                }
            }
        }

        LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
        LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            -> {
            CoverGridOverlay(dragHandle = dragHandle) {
                CoverOnlyCell(
                    modifier = entryModifier,
                    onClick = { onEditionClick(edition) },
                ) { coverModifier ->
                    EditionImage(
                        edition = edition,
                        modifier = coverModifier,
                        isLoading = false,
                        defaultEdition = edition,
                        coverlessTitle = title,
                        elevation = 6.dp,
                        cornerRadius = 10.dp,
                        sharedTransitionKey = bookCoverTransitionKey(
                            editionId = edition.id,
                            bookId = edition.bookId,
                            surface = "edition-${edition.id}",
                        ),
                    )
                }
            }
        }

        LibraryGridLayout.LIST_COMPACT -> {
            CompactRow(
                modifier = entryModifier,
                title = title,
                authorName = authorName,
                onClick = { onEditionClick(edition) },
                trailing = dragHandle,
            )
        }

        LibraryGridLayout.LIST_LARGE -> {
            LargeRow(
                modifier = entryModifier,
                title = title,
                authorName = authorName,
                onClick = { onEditionClick(edition) },
                trailing = dragHandle,
            ) { coverModifier ->
                EditionImage(
                    edition = edition,
                    modifier = coverModifier,
                    isLoading = false,
                    defaultEdition = edition,
                    elevation = 6.dp,
                    cornerRadius = 10.dp,
                    sharedTransitionKey = bookCoverTransitionKey(
                        editionId = edition.id,
                        bookId = edition.bookId,
                        surface = "edition-${edition.id}",
                    ),
                )
            }
        }
    }
}

@Composable
private fun CoverOnlyCell(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    cover: @Composable (Modifier) -> Unit,
) {
    cover(
        modifier
            .fillMaxWidth()
            .aspectRatio(ratio = 2f / 3f)
            .pointerHandCursor()
            .pressScaleCombinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    )
}

@Composable
private fun GridBookCell(
    title: String,
    authorName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    cover: @Composable (Modifier) -> Unit,
) {
    Column(
        modifier = modifier
            .pointerHandCursor()
            .pressScaleCombinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        cover(
            Modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 2f / 3f),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = title,
            style = MaterialTheme.editorialTypography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        if (authorName.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = authorName,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CompactRow(
    title: String,
    authorName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    deadlineProgress: DeadlineProgress? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerHandCursor()
            .pressScaleCombinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSelectionMode) {
                SelectionLeadingIcon(isSelected = isSelected)

                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.editorialTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (authorName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "By $authorName",
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (deadlineProgress != null) {
                Spacer(modifier = Modifier.width(8.dp))

                DeadlineBadge(status = deadlineProgress.status)
            }

            if (trailing != null) {
                Spacer(modifier = Modifier.width(8.dp))

                trailing()
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun LargeRow(
    title: String,
    authorName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    seriesText: String? = null,
    releaseYear: Int? = null,
    usersCount: Int? = null,
    rating: Double? = null,
    deadlineProgress: DeadlineProgress? = null,
    dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
    trailing: (@Composable () -> Unit)? = null,
    cover: @Composable (Modifier) -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .pointerHandCursor()
            .pressScaleCombinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            cover(
                Modifier
                    .width(96.dp)
                    .aspectRatio(ratio = 2f / 3f),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                seriesText?.takeIf { it.isNotBlank() }?.let { series ->
                    Text(
                        text = series.uppercase(),
                        style = MaterialTheme.editorialTypography.eyebrowSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.editorialTypography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (authorName.isNotBlank()) {
                    Text(
                        text = "By $authorName",
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                val hasRating = rating != null && rating != 0.0

                val statsLabel = listOfNotNull(
                    releaseYear?.takeIf { it != -1 }?.toString(),
                    usersCount?.let { "$it readers" },
                    rating?.takeIf { it != 0.0 }?.toString(),
                ).joinToString(separator = " • ")

                if (statsLabel.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = statsLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        if (hasRating) {
                            Spacer(modifier = Modifier.width(4.dp))

                            val starIcon = SoftcoverIconResource.Drawable(
                                icon = SoftcoverIcon.StarFilled,
                                contentDescription = "",
                            )

                            Icon(
                                painter = starIcon.getIconPainter(),
                                contentDescription = starIcon.contentDescription,
                                tint = RatingGold,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }

                if (deadlineProgress != null) {
                    Spacer(modifier = Modifier.height(2.dp))

                    DeadlineSummaryLine(
                        progress = deadlineProgress,
                        dateStyle = dateStyle,
                    )
                }
            }

            if (trailing != null) {
                Spacer(modifier = Modifier.width(8.dp))

                trailing()
            }
        }
    }
}

@Composable
private fun SelectableCover(
    isSelectionMode: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    alpha = if (isSelectionMode && isSelected.not()) UNSELECTED_COVER_ALPHA else 1f
                },
        ) {
            content()
        }

        if (isSelectionMode) {
            SelectionCircleIndicator(
                isSelected = isSelected,
                unselectedContainer = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun SelectionLeadingIcon(isSelected: Boolean) {
    SelectionCircleIndicator(
        isSelected = isSelected,
        unselectedContainer = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
}

@Composable
private fun SelectionCircleIndicator(
    isSelected: Boolean,
    unselectedContainer: Color,
    modifier: Modifier = Modifier,
) {
    val container = if (isSelected) MaterialTheme.colorScheme.primary else unselectedContainer

    val content = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(percent = 50),
        modifier = modifier.size(24.dp),
    ) {
        if (isSelected) {
            val checkIcon = SoftcoverIconResource.Drawable(
                icon = SoftcoverIcon.Check,
                contentDescription = "",
            )

            Icon(
                painter = checkIcon.getIconPainter(),
                contentDescription = checkIcon.contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
            )
        }
    }
}

@Composable
private fun EmptyListScreen(tab: LibraryContentTab) {
    val isDnf = tab is LibraryContentTab.Status &&
            tab.status == UserBookStatus.DID_NOT_FINISH

    val headline = if (isDnf) "Nothing set aside" else "An empty shelf"

    val body = if (isDnf) {
        "No books abandoned here — long may it stay that way."
    } else {
        "Nothing rests on your ${tab.label} list yet. Find a title worth keeping and it will live here."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val quoteAlpha = if (isSystemInDarkTheme()) 0.12f else 0.25f

        Text(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph,
            color = MaterialTheme.colorScheme.primary.copy(alpha = quoteAlpha),
            modifier = Modifier
                .padding(top = 8.dp)
                .quoteGlyphSway(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = headline,
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

internal fun subtitleFor(
    tab: LibraryContentTab?,
    bookCount: Int?,
    totalPages: Int,
): String {
    if (bookCount == null) return "Loading your shelf…"
    if (bookCount == 0) {
        return when {
            tab is LibraryContentTab.Status && tab.status == UserBookStatus.READ ->
                "Nothing finished yet — the page is still open."
            tab is LibraryContentTab.Status && tab.status == UserBookStatus.WANT_TO_READ ->
                "No titles set aside — discover one next."
            tab is LibraryContentTab.Status && tab.status == UserBookStatus.CURRENTLY_READING ->
                "No book open — pick one up."
            else -> "No titles yet — your story starts here."
        }
    }

    val titlesPart = formatBookCount(count = bookCount)
    val pagesPart = formatPageCount(pages = totalPages)

    return if (pagesPart != null) "$titlesPart · $pagesPart" else titlesPart
}

private const val UNSELECTED_COVER_ALPHA = 0.55f
// endregion
// region Shared controls
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ShelfTabRow(
    tabs: List<LibraryContentTab>,
    currentPage: Int,
    maxLabelWidth: Dp,
    onTabClick: (Int) -> Unit,
    onTabLongPress: () -> Unit,
) {
    val density = LocalDensity.current
    val peekPx = with(density) { 48.dp.toPx() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = currentPage == index
            val requester = remember { BringIntoViewRequester() }
            var pillSize by remember { mutableStateOf(IntSize.Zero) }

            LaunchedEffect(selected, pillSize) {
                if (selected && pillSize.width > 0) {
                    requester.bringIntoView(
                        Rect(
                            left = -peekPx,
                            top = 0f,
                            right = pillSize.width + peekPx,
                            bottom = pillSize.height.toFloat(),
                        ),
                    )
                }
            }

            ShelfTabPill(
                label = tab.label,
                selected = selected,
                maxLabelWidth = maxLabelWidth,
                onClick = { onTabClick(index) },
                onLongClick = onTabLongPress,
                modifier = Modifier
                    .bringIntoViewRequester(requester)
                    .onSizeChanged { pillSize = it },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShelfTabPill(
    label: String,
    selected: Boolean,
    maxLabelWidth: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val content = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val haptics = LocalHaptics.current
    val interactionSource = remember { MutableInteractionSource() }
    val pillShape = RoundedCornerShape(percent = 50)

    Surface(
        modifier = modifier
            .pointerHandCursor()
            .hoverHighlight(
                interactionSource = interactionSource,
                shape = pillShape,
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = {
                    haptics.threshold()

                    onLongClick()
                },
            ),
        color = container,
        contentColor = content,
        shape = pillShape,
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
            modifier = Modifier
                .widthIn(max = maxLabelWidth)
                .padding(horizontal = 18.dp, vertical = 10.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ReadYearChipRow(
    years: List<Int>,
    selectedYear: Int?,
    onYearClick: (Int?) -> Unit,
) {
    // Wraps on a fixed-width desktop pane (a pointer can't fling a chip row sideways); compact and
    // medium keep the horizontal scroll.
    val wrap = rememberWindowSizeClass().widthClass == WindowWidthClass.EXPANDED

    val chipContent: @Composable () -> Unit = {
        YearChip(
            label = "All years",
            selected = selectedYear == null,
            onClick = { onYearClick(null) },
        )

        years.forEach { year ->
            YearChip(
                label = year.toString(),
                selected = selectedYear == year,
                onClick = { onYearClick(year) },
            )
        }
    }

    if (wrap) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            chipContent()
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            chipContent()
        }
    }
}

@Composable
private fun YearChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val container = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val content = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(percent = 50),
        onClick = onClick,
        modifier = Modifier.pointerHandCursor(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
// endregion
// region Selection mode
@Composable
internal fun SelectionHeader(
    selectedCount: Int,
    bulkActionInProgress: Boolean,
    isMoveMenuExpanded: Boolean,
    onExit: () -> Unit,
    onMoveMenuExpandedChange: (Boolean) -> Unit,
    onMoveShelf: (UserBookStatus) -> Unit,
    onAddToListClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onExit,
                enabled = bulkActionInProgress.not(),
                modifier = Modifier.pointerHandCursor(),
            ) {
                val exitIcon = SoftcoverIconResource.Drawable(
                    icon = SoftcoverIcon.Close,
                    contentDescription = "Exit selection mode",
                )

                Icon(
                    painter = exitIcon.getIconPainter(),
                    contentDescription = exitIcon.contentDescription,
                )
            }

            Text(
                text = "$selectedCount selected",
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            )

            Box {
                IconButton(
                    onClick = { onMoveMenuExpandedChange(true) },
                    enabled = bulkActionInProgress.not(),
                    modifier = Modifier.pointerHandCursor(),
                ) {
                    val moveIcon = SoftcoverIconResource.Drawable(
                        icon = SoftcoverIcon.Bookmark,
                        contentDescription = "Move selected books to another shelf",
                    )

                    Icon(
                        painter = moveIcon.getIconPainter(),
                        contentDescription = moveIcon.contentDescription,
                    )
                }

                DropdownMenu(
                    expanded = isMoveMenuExpanded,
                    onDismissRequest = { onMoveMenuExpandedChange(false) },
                ) {
                    SelectionShelfTargets.forEach { (status, label) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                            onClick = { onMoveShelf(status) },
                        )
                    }
                }
            }

            IconButton(
                onClick = onAddToListClick,
                enabled = bulkActionInProgress.not(),
                modifier = Modifier.pointerHandCursor(),
            ) {
                val addToListIcon = SoftcoverIconResource.Drawable(
                    icon = SoftcoverIcon.BookmarkAdd,
                    contentDescription = "Add selected books to a list",
                )

                Icon(
                    painter = addToListIcon.getIconPainter(),
                    contentDescription = addToListIcon.contentDescription,
                )
            }

            IconButton(
                onClick = onRemoveClick,
                enabled = bulkActionInProgress.not(),
                modifier = Modifier.pointerHandCursor(),
            ) {
                val removeIcon = SoftcoverIconResource.Drawable(
                    icon = SoftcoverIcon.Delete,
                    contentDescription = "Remove selected books from library",
                )

                Icon(
                    painter = removeIcon.getIconPainter(),
                    contentDescription = removeIcon.contentDescription,
                )
            }
        }
    }
}

@Composable
internal fun BulkRemoveConfirmationDialog(
    bookCount: Int,
    inProgress: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val titlePlural = if (bookCount == 1) "book" else "books"

    // A transient destructive confirm is a bare overlay (§6) rather than a sheet, but it still
    // carries the editorial register — Fraunces headline + body and the shared button — instead of
    // Material AlertDialog chrome.
    Dialog(
        onDismissRequest = {
            if (inProgress.not()) onDismiss()
        },
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Remove $bookCount $titlePlural?",
                    style = MaterialTheme.editorialTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "They'll come off every shelf and out of your Hardcover library. " +
                        "You can always add them again later.",
                    style = MaterialTheme.editorialTypography.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.End,
                    ),
                ) {
                    SoftcoverButton(
                        label = "Keep",
                        style = ButtonStyle.TEXT,
                        onClick = onDismiss,
                        enabled = inProgress.not(),
                    )

                    SoftcoverButton(
                        label = "Remove",
                        style = ButtonStyle.TEXT,
                        onClick = onConfirm,
                        enabled = inProgress.not(),
                    )
                }
            }
        }
    }
}

private val SelectionShelfTargets: List<Pair<UserBookStatus, String>> = listOf(
    UserBookStatus.WANT_TO_READ to "Move to Want to Read",
    UserBookStatus.CURRENTLY_READING to "Move to Currently Reading",
    UserBookStatus.READ to "Mark as Read",
)
// endregion
