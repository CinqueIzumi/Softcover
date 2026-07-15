package nl.rhaydus.softcover.feature.library.presentation.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.editorial.component.EditorialSearchField
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.modifier.dismissOnEscape
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.component.ChooseListsBottomSheet
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab as LibraryContentTab
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnArrangeSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkAddToListSheetShownAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkMoveMenuExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkMoveShelfAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveDialogExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveFromLibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkToggleListMembershipAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnClearFiltersAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnExitRearrangeModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnExitSelectionModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnFilterSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSearchQueryChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnTabSelectedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnToggleFilterValueAction
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryArrangeSheet
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryControlLine
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryFilterChipRow
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryFilterSheet
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.util.totalPages

/**
 * Desktop Library: a vertical shelf **source list** (the native desktop pattern for switching
 * collections) down the leading edge, beside a wide pane-width-adaptive cover grid with a persistent
 * desktop scrollbar. The header is static (no touch-collapse) and refresh is an explicit control;
 * there is no pager and no pull-to-refresh. Unlike mobile, the sidebar stays the switcher (no
 * title-chevron, no Shelves sheet — see design-system.md); the masthead control line and Arrange sheet
 * are otherwise shared with mobile. Selection mode, filtering, and the bulk sheets reuse the shared
 * shelf components verbatim ([SelectionHeader], [BookList], [EditionList], [LibraryFilterSheet]).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun LibraryScreenLayout(
    state: LibraryUiState,
    runAction: (LibraryAction) -> Unit,
    onBookClick: (Book) -> Unit,
    onEditionClick: (BookEdition) -> Unit,
    onTabLongPress: () -> Unit,
    onCreateNewListClick: () -> Unit,
    gridStateFor: (String) -> LazyGridState,
    topAppBarState: TopAppBarState,
) {
    val tabs = state.visibleTabs
    val haptics = LocalHaptics.current

    val currentTab = tabs.firstOrNull { it.id == state.selectedTabId } ?: tabs.firstOrNull()

    val currentTabBooks: List<Book>? = currentTab?.let { tab ->
        when (tab) {
            is LibraryContentTab.CustomList -> null
            is LibraryContentTab.All,
            is LibraryContentTab.Status,
                -> state.displayBooksFor(tabId = tab.id)
        }
    }

    val currentTabEditions: List<BookEdition>? = currentTab?.let { tab ->
        when (tab) {
            is LibraryContentTab.CustomList -> state.displayEditionsFor(tabId = tab.id)
            else -> null
        }
    }

    val currentTabBookCount = currentTabBooks?.size ?: currentTabEditions?.size
    val currentTabPageCount = currentTabBooks?.totalPages() ?: 0

    val selectionBackState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = selectionBackState,
        isBackEnabled = state.selectionMode,
        onBackCancelled = {},
        onBackCompleted = { runAction(OnExitSelectionModeAction()) },
    )

    val rearrangeBackState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = rearrangeBackState,
        isBackEnabled = state.isRearranging,
        onBackCancelled = {},
        onBackCompleted = { runAction(OnExitRearrangeModeAction()) },
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .dismissOnEscape(enabled = state.selectionMode || state.isRearranging) {
                if (state.selectionMode) {
                    runAction(OnExitSelectionModeAction())
                } else {
                    runAction(OnExitRearrangeModeAction())
                }
            },
    ) {
        ShelfSidebar(
            tabs = tabs,
            selectedTabId = currentTab?.id,
            onTabClick = { id -> runAction(OnTabSelectedAction(tabId = id)) },
            onTabLongPress = onTabLongPress,
            modifier = Modifier
                .width(SHELF_SIDEBAR_WIDTH)
                .fillMaxHeight(),
        )

        VerticalDivider()

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            if (state.selectionMode) {
                SelectionHeader(
                    selectedCount = state.selectedBookIds.size,
                    bulkActionInProgress = state.bulkActionInProgress,
                    isMoveMenuExpanded = state.isBulkMoveMenuExpanded,
                    onExit = { runAction(OnExitSelectionModeAction()) },
                    onMoveMenuExpandedChange = { expanded ->
                        runAction(OnBulkMoveMenuExpandedChangeAction(expanded = expanded))
                    },
                    onMoveShelf = { status ->
                        haptics.commit()

                        runAction(OnBulkMoveShelfAction(status = status))
                    },
                    onAddToListClick = {
                        runAction(OnBulkAddToListSheetShownAction(shown = true))
                    },
                    onRemoveClick = {
                        runAction(OnBulkRemoveDialogExpandedChangeAction(shown = true))
                    },
                )
            } else {
                DesktopLibraryHeader(
                    tabLabel = currentTab?.label,
                    tab = currentTab,
                    bookCount = currentTabBookCount,
                    totalPages = currentTabPageCount,
                    isRefreshing = state.isLoading,
                    onRefreshClick = { runAction(OnRefreshAction()) },
                )

                // Search is persistent (no toggle) per the redesign.
                Spacer(modifier = Modifier.height(4.dp))

                EditorialSearchField(
                    query = state.searchQuery,
                    onQueryChange = { query ->
                        runAction(OnSearchQueryChangeAction(query = query))
                    },
                    onClearClick = {
                        runAction(OnSearchQueryChangeAction(query = ""))
                    },
                    searchIcon = drawableIconResource(
                        icon = SoftcoverIcon.Search,
                        contentDescription = "Search",
                    ),
                    clearIcon = drawableIconResource(
                        icon = SoftcoverIcon.Close,
                        contentDescription = "Clear search",
                    ),
                    placeholder = "Search your collection",
                )

                Spacer(modifier = Modifier.height(16.dp))

                LibraryControlLine(
                    state = state,
                    tab = currentTab,
                    runAction = runAction,
                )

                val activeFilters = currentTab?.id?.let { state.filtersFor(tabId = it) }

                if (currentTab != null && activeFilters != null && activeFilters.isEmpty.not()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    LibraryFilterChipRow(
                        filters = activeFilters,
                        onRemove = { value ->
                            runAction(
                                OnToggleFilterValueAction(
                                    tabId = currentTab.id,
                                    value = value,
                                ),
                            )
                        },
                        onClearAll = {
                            runAction(OnClearFiltersAction(tabId = currentTab.id))
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (currentTab != null) {
                    val gridState = gridStateFor(currentTab.id)
                    val columns = desktopGridColumns(layout = state.gridLayout)

                    when (currentTab) {
                        is LibraryContentTab.CustomList -> EditionList(
                            tab = currentTab,
                            state = state,
                            gridState = gridState,
                            onEditionClick = onEditionClick,
                            runAction = runAction,
                            columnsOverride = columns,
                        )

                        is LibraryContentTab.All,
                        is LibraryContentTab.Status,
                            -> BookList(
                                tab = currentTab,
                                state = state,
                                gridState = gridState,
                                onBookClick = onBookClick,
                                runAction = runAction,
                                columnsOverride = columns,
                            )
                    }

                    DesktopVerticalScrollbar(
                        gridState = gridState,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp),
                    )
                }
            }
        }
    }

    if (state.isArrangeSheetExpanded && currentTab != null) {
        LibraryArrangeSheet(
            tab = currentTab,
            state = state,
            runAction = runAction,
            onDismissRequest = {
                runAction(OnArrangeSheetExpandedChangeAction(expanded = false))
            },
        )
    }

    if (state.isFilterSheetExpanded && currentTab != null) {
        LibraryFilterSheet(
            tabId = currentTab.id,
            state = state,
            runAction = runAction,
            onDismissRequest = {
                runAction(OnFilterSheetExpandedChangeAction(expanded = false))
            },
        )
    }

    if (state.isBulkRemoveDialogShown) {
        BulkRemoveConfirmationDialog(
            bookCount = state.selectedBookIds.size,
            inProgress = state.bulkActionInProgress,
            onConfirm = {
                haptics.commit()

                runAction(OnBulkRemoveFromLibraryAction())
            },
            onDismiss = {
                runAction(OnBulkRemoveDialogExpandedChangeAction(shown = false))
            },
        )
    }

    if (state.isBulkAddToListSheetShown && state.selectedBookIds.isNotEmpty()) {
        ChooseListsBottomSheet(
            bookIds = state.selectedBookIds,
            customLists = state.customLists.filter { it.isOwned.not() },
            listsBeingMutated = state.listsBeingMutated,
            onDismissRequest = {
                runAction(OnBulkAddToListSheetShownAction(shown = false))
            },
            onToggleMembership = { listId, membership ->
                haptics.commit()

                runAction(
                    OnBulkToggleListMembershipAction(
                        listId = listId,
                        currentMembership = membership,
                    ),
                )
            },
            onCreateNewListClick = onCreateNewListClick,
        )
    }
}

/**
 * The desktop shelf source list: built-in shelves grouped first, then a "Lists" section for the
 * user's custom lists. Each entry is a full-width row whose long name ellipsizes (no horizontal
 * scroll or wrapping to fight). The whole rail scrolls vertically — a mouse wheel drives it natively.
 * A long-press routes to the shelf-visibility settings, mirroring the mobile Shelves sheet's own
 * "Manage" affordance. Desktop keeps this permanent sidebar rather than the mobile title-as-switcher
 * + Shelves sheet — a wide window already has room for a source list, which is the native desktop
 * switcher pattern (design-system.md §2.7/§5).
 */
@Composable
private fun ShelfSidebar(
    tabs: List<LibraryContentTab>,
    selectedTabId: String?,
    onTabClick: (String) -> Unit,
    onTabLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shelves = tabs.filter { it !is LibraryContentTab.CustomList }
    val lists = tabs.filterIsInstance<LibraryContentTab.CustomList>()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                top = 16.dp,
                bottom = 16.dp + rememberBottomBarPadding(),
            ),
    ) {
        SidebarSectionLabel(text = "Shelves")

        shelves.forEach { tab ->
            ShelfSidebarRow(
                label = tab.label,
                selected = tab.id == selectedTabId,
                onClick = { onTabClick(tab.id) },
                onLongClick = onTabLongPress,
            )
        }

        if (lists.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            SidebarSectionLabel(text = "Lists")

            lists.forEach { tab ->
                ShelfSidebarRow(
                    label = tab.label,
                    selected = tab.id == selectedTabId,
                    onClick = { onTabClick(tab.id) },
                    onLongClick = onTabLongPress,
                )
            }
        }
    }
}

@Composable
private fun SidebarSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrowSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 6.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShelfSidebarRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val container = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent

    val content = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val haptics = LocalHaptics.current
    val interactionSource = remember { MutableInteractionSource() }
    val rowShape = RoundedCornerShape(10.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .pointerHandCursor()
            .hoverHighlight(
                interactionSource = interactionSource,
                shape = rowShape,
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
        shape = rowShape,
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
        )
    }
}

@Composable
private fun DesktopLibraryHeader(
    tabLabel: String?,
    tab: LibraryContentTab?,
    bookCount: Int?,
    totalPages: Int,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, top = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isRefreshing) "Refreshing your shelf…" else "Your collection",
                style = MaterialTheme.editorialTypography.eyebrow,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tabLabel ?: "Library",
                style = MaterialTheme.editorialTypography.pageTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 16.dp),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitleFor(
                    tab = tab,
                    bookCount = bookCount,
                    totalPages = totalPages,
                ),
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextButton(
                onClick = onRefreshClick,
                enabled = isRefreshing.not(),
                modifier = Modifier.pointerHandCursor(),
            ) {
                Text(text = if (isRefreshing) "Refreshing…" else "Refresh")
            }
        }
    }
}

/**
 * Desktop column strategy: cover grids fill the content width adaptively (more columns on a wider
 * window) rather than honouring the phone-tuned fixed 2/3 split, while list layouts stay single
 * column. Titled and cover-only grids share the same cover footprint (`minSize`) so a cover is never
 * smaller without its caption — dropping the caption only makes the cover-only grid denser *vertically*
 * (shorter cells), not horizontally; with its tighter item spacing it ends up a touch larger, never
 * smaller.
 */
private fun desktopGridColumns(layout: LibraryGridLayout): GridCells = when (layout) {
    LibraryGridLayout.GRID_TWO_COLUMNS,
    LibraryGridLayout.GRID_THREE_COLUMNS,
    LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
    LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
        -> GridCells.Adaptive(minSize = 150.dp)

    LibraryGridLayout.LIST_COMPACT,
    LibraryGridLayout.LIST_LARGE,
        -> GridCells.Fixed(count = 1)
}

private val SHELF_SIDEBAR_WIDTH = 208.dp
