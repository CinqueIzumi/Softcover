package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.testTag
import nl.rhaydus.designsystem.component.DesktopContextMenu
import nl.rhaydus.designsystem.component.DesktopContextMenuItem
import nl.rhaydus.designsystem.component.rememberLazyItemMutationAnimator
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.modifier.platformModifierClick
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.bookCardHandOver
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.bookcard.rememberBookCardEntryCoordinator
import nl.rhaydus.softcover.core.component.haptics.rememberReorderHaptics
import nl.rhaydus.softcover.core.component.reorder.reorderLift
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab as LibraryContentTab
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkAddToListSheetShownAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkMoveShelfAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveFromLibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnEnterSelectionModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnReorderShelfBooksAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSelectBookRangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnToggleBookSelectionAction
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.bookReorderStatusFor
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

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

    if (rawBooks != null && rawBooks.isEmpty() && state.isLoading.not()) {
        state.emptyStatesByTab[tab.id]?.let { emptyState ->
            EmptyState(
                model = emptyState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = Spacing.gutter,
                        end = Spacing.gutter,
                        bottom = rememberBottomBarPadding(),
                    ),
            )
        }

        return
    }

    val tabCards = state.bookCardsByTab[tab.id] ?: state.loadingCards

    val cardSet = tabCards.set
    val cards = cardSet.cards

    // Books arrive pre-sorted from the DAO via SQL ORDER BY; DisplayListsCollector applies the
    // search + Read-tab year filter off the main thread, and BookCardModelsCollector builds the
    // cards from that result. The cards carry the order and their own books, so a card's event or
    // menu resolves to the book it was built from even while the live lists are ahead of the cards.
    val visibleKeys = remember(cards) { cards.map { it.key.lazyKey } }

    val bookIdByLazyKey = remember(cards) {
        cards.mapNotNull { card ->
            (card.key as? BookCardKey.Book)?.takeUnless { card.isLoading }?.let { card.key.lazyKey to it.bookId }
        }.toMap()
    }

    val isShowingStandIns = remember(cards) { cards.any { it.isLoading } }

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
    val reorderStatus: UserBookStatus? = bookReorderStatusFor(
        tab = tab,
        sortMode = sortMode,
        selectionMode = selectionMode,
        isRearranging = state.isRearranging,
    )

    val isRearranging = reorderStatus != null && isShowingStandIns.not()

    val realKeys = remember(cards) { cards.filterNot { it.isLoading }.map { it.key.lazyKey } }

    val animator = rememberLazyItemMutationAnimator(keys = realKeys)

    val entry = rememberBookCardEntryCoordinator(
        cards = cards,
        key = "library:books:${tab.id}",
    )

    ScrollToTopOnVisibleSetChange(
        tabId = tab.id,
        sortMode = sortMode,
        sortDirection = state.sortDirectionFor(tabId = tab.id),
        filters = state.filtersFor(tabId = tab.id),
        visibleItemsKey = visibleKeys.firstOrNull()?.let(bookIdByLazyKey::get) ?: 0,
        gridState = gridState,
    )

    val haptics = LocalHaptics.current
    val reorderHaptics = rememberReorderHaptics()

    // One handler for the whole grid; the cards route their events here by key.
    val onCardEvent = rememberPrefetchingBookCardEventHandler(
        set = cardSet,
        prefetchEnabled = selectionMode.not(),
    ) { event, book ->
        when (event) {
            is BookCardEvent.Clicked -> if (selectionMode) {
                runAction(OnToggleBookSelectionAction(bookId = book.id))
            } else {
                onBookClick(book)
            }

            is BookCardEvent.LongClicked -> {
                haptics.threshold()

                runAction(OnEnterSelectionModeAction(bookId = book.id))
            }

            is BookCardEvent.PressStarted,
            is BookCardEvent.TrailingClicked,
            is BookCardEvent.MenuClicked,
            is BookCardEvent.ShakeEnded,
                -> Unit
        }
    }

    // Live shadow the reorder library mutates during a drag. Eagerly seeded and kept in
    // lock-step with the canonical (DB-sorted) list so the first frame is already correct and
    // toggling rearrange never blanks the grid; it re-syncs whenever the canonical list changes
    // (e.g. a book shelved or unshelved from elsewhere).
    val orderedKeys = remember { visibleKeys.toMutableStateList() }

    LaunchedEffect(visibleKeys) {
        if (orderedKeys.toList() != visibleKeys) {
            orderedKeys.clear()
            orderedKeys.addAll(visibleKeys)
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
        val fromIndex = orderedKeys.indexOf(from.key as String)
        val toIndex = orderedKeys.indexOf(to.key as String)

        if (fromIndex == -1 || toIndex == -1) return@rememberReorderableLazyGridState

        orderedKeys.add(
            toIndex,
            orderedKeys.removeAt(fromIndex),
        )

        reorderHaptics.moved(toIndex)

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
    val renderKeys = if (isRearranging) orderedKeys else visibleKeys

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
            contentType = { _, _ -> "book" },
        ) { index, card ->
            val gridItemScope = this

            val bookId = (card.key as? BookCardKey.Book)?.bookId ?: return@itemsIndexed

            ReorderableItem(state = reorderableState, key = card.key.lazyKey) { isDragging ->
                // Built unconditionally so the per-item composable structure is identical in and
                // out of rearrange mode — only the drag-handle slot is swapped below (a parameter
                // value, not a composable call), so the cover is never remounted (no flash).
                val handleModifier = Modifier.draggableHandle(
                    onDragStarted = {
                        maxTouchedIndex.intValue = -1

                        reorderHaptics.lift()
                    },
                    onDragStopped = {
                        reorderHaptics.drop()

                        val touchedDepth = maxTouchedIndex.intValue

                        if (reorderStatus != null && touchedDepth >= 0 && touchedDepth < orderedKeys.size) {
                            runAction(
                                OnReorderShelfBooksAction(
                                    status = reorderStatus,
                                    prefixOrderedBookIds = orderedKeys
                                        .take(touchedDepth + 1)
                                        .mapNotNull(bookIdByLazyKey::get),
                                ),
                            )
                        }
                    },
                )

                val isSelected = selectionMode && bookId in state.selectedBookIds

                // Desktop selection affordances, inert on touch: Ctrl/Cmd-click toggles this cover's
                // selection (entering selection mode if needed), Shift-click range-selects the
                // visible span up to the anchor. Suppressed in rearrange mode (drag-only).
                val onCtrlClick = {
                    selectionAnchorId = bookId

                    if (selectionMode) {
                        runAction(OnToggleBookSelectionAction(bookId = bookId))
                    } else {
                        runAction(OnEnterSelectionModeAction(bookId = bookId))
                    }
                }

                val onShiftClick = {
                    val anchor = selectionAnchorId

                    if (anchor == null) {
                        selectionAnchorId = bookId

                        runAction(OnEnterSelectionModeAction(bookId = bookId))
                    } else {
                        runAction(
                            OnSelectBookRangeAction(
                                bookIds = idRangeBetween(
                                    ids = renderKeys.mapNotNull(bookIdByLazyKey::get),
                                    anchorId = anchor,
                                    targetId = bookId,
                                ),
                            ),
                        )
                    }
                }

                // Desktop right-click menu (empty in rearrange mode → pass-through, and a no-op on
                // touch where selection is reached via long-press instead).
                val contextMenuItems = if (isRearranging || isShowingStandIns) {
                    emptyList()
                } else {
                    libraryBookContextMenu(
                        isRead = tabCards.menuByBookId[bookId]?.isRead == true,
                        isPaused = tabCards.menuByBookId[bookId]?.isPaused == true,
                        selectionMode = selectionMode,
                        isSelected = isSelected,
                        onOpen = { cardSet[card.key]?.let(onBookClick) },
                        onSelect = onCtrlClick,
                        onToggleSelection = {
                            haptics.select()

                            runAction(OnToggleBookSelectionAction(bookId = bookId))
                        },
                        onMarkAsRead = {
                            // In selection mode the menu acts on the whole selection (null →
                            // current selection); otherwise on just the right-clicked book.
                            val targetIds = if (selectionMode) null else setOf(bookId)

                            runAction(
                                OnBulkMoveShelfAction(
                                    status = UserBookStatus.READ,
                                    explicitBookIds = targetIds,
                                ),
                            )
                        },
                        onResumeReading = {
                            runAction(
                                OnBulkMoveShelfAction(
                                    status = UserBookStatus.CURRENTLY_READING,
                                    explicitBookIds = setOf(bookId),
                                ),
                            )
                        },
                        onAddToList = {
                            // Enter-selection commits its setState before the sheet-shown action
                            // runs (TOAD dispatches actions FIFO on one scope and enter-selection
                            // has no suspension point before its setState), so the sheet opens with
                            // this book already selected.
                            if (selectionMode.not()) {
                                runAction(OnEnterSelectionModeAction(bookId = bookId))
                            }

                            runAction(OnBulkAddToListSheetShownAction(shown = true))
                        },
                        onRemove = {
                            val targetIds = if (selectionMode) null else setOf(bookId)

                            runAction(OnBulkRemoveFromLibraryAction(explicitBookIds = targetIds))
                        },
                    )
                }

                val baseModifier = Modifier.bookCardHandOver(
                    scope = gridItemScope,
                    card = card,
                    animator = animator,
                    coordinator = entry,
                    index = index,
                )

                val entryModifier = if (isRearranging || isShowingStandIns) {
                    baseModifier
                } else {
                    baseModifier.platformModifierClick(
                        onCtrlClick = onCtrlClick,
                        onShiftClick = onShiftClick,
                    )
                }.reorderLift(
                    isDragging = isDragging,
                    outset = Spacing.xs,
                )

                DesktopContextMenu(items = contextMenuItems) {
                    BookCard(
                        model = card,
                        onEvent = onCardEvent,
                        modifier = entryModifier.testTag(LibraryTestTags.BOOK_CARD),
                        content = {
                            DragHandle(
                                visible = isRearranging,
                                modifier = handleModifier,
                            )
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
 * that reuse the bulk pipeline on a single book (Mark as read — hidden when already Read — Resume
 * reading — only on a Paused book — Add to list, Remove). Empty on touch (the caller passes an empty
 * list outside desktop / in rearrange mode).
 */
private fun libraryBookContextMenu(
    isRead: Boolean,
    isPaused: Boolean,
    selectionMode: Boolean,
    isSelected: Boolean,
    onOpen: () -> Unit,
    onSelect: () -> Unit,
    onToggleSelection: () -> Unit,
    onMarkAsRead: () -> Unit,
    onResumeReading: () -> Unit,
    onAddToList: () -> Unit,
    onRemove: () -> Unit,
): List<DesktopContextMenuItem> = buildList {
    add(
        DesktopContextMenuItem(
            label = "Open",
            onClick = onOpen,
        ),
    )

    if (selectionMode) {
        val toggleLabel = if (isSelected) "Remove from selection" else "Add to selection"

        add(
            DesktopContextMenuItem(
                label = toggleLabel,
                onClick = onToggleSelection,
            ),
        )
    } else {
        add(
            DesktopContextMenuItem(
                label = "Select",
                onClick = onSelect,
            ),
        )
    }

    if (isRead.not()) {
        add(
            DesktopContextMenuItem(
                label = "Mark as read",
                onClick = onMarkAsRead,
            ),
        )
    }

    if (isPaused) {
        add(
            DesktopContextMenuItem(
                label = "Resume reading",
                onClick = onResumeReading,
            ),
        )
    }

    add(
        DesktopContextMenuItem(
            label = "Add to list…",
            onClick = onAddToList,
        ),
    )
    add(
        DesktopContextMenuItem(
            label = "Remove from library",
            onClick = onRemove,
        ),
    )
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
