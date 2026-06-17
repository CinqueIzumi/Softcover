package nl.rhaydus.softcover.feature.library.presentation.screen

import nl.rhaydus.designsystem.haptics.LocalHaptics
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.dismissOnEscape
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.component.ChooseListsBottomSheet
import nl.rhaydus.softcover.core.designsystem.presentation.component.DesktopTooltip
import nl.rhaydus.softcover.core.designsystem.presentation.component.DesktopVerticalScrollbar
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditorialSearchField
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab as LibraryContentTab
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
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
import nl.rhaydus.softcover.feature.library.presentation.action.OnReadYearSelectedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSearchQueryChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnTabSelectedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnToggleFilterValueAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnToggleSearchAction
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryControlStrip
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryFilterChipRow
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryFilterSheet
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.util.totalPages

/**
 * Desktop Library: a vertical shelf **source list** (the native desktop pattern for switching
 * collections) down the leading edge, beside a wide pane-width-adaptive cover grid with a persistent
 * desktop scrollbar. The header is static (no touch-collapse) and refresh is an explicit control;
 * there is no pager and no pull-to-refresh. Selection mode, filtering, and the bulk sheets reuse the
 * shared shelf components verbatim ([SelectionHeader], [BookList], [EditionList], [LibraryFilterSheet]).
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
    val isReadTab = currentTab is LibraryContentTab.Status &&
            currentTab.status == UserBookStatus.READ

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

    val availableReadYears = if (isReadTab) state.availableReadYears else emptyList()

    BackHandler(enabled = state.selectionMode) {
        runAction(OnExitSelectionModeAction())
    }

    BackHandler(enabled = state.isRearranging) {
        runAction(OnExitRearrangeModeAction())
    }

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
                    isSearchActive = state.isSearchActive,
                    isRefreshing = state.isLoading,
                    onToggleSearchClick = { runAction(OnToggleSearchAction()) },
                    onRefreshClick = { runAction(OnRefreshAction()) },
                )

                if (state.isSearchActive) {
                    Spacer(modifier = Modifier.height(4.dp))

                    EditorialSearchField(
                        query = state.searchQuery,
                        onQueryChange = { query ->
                            runAction(OnSearchQueryChangeAction(query = query))
                        },
                        onClearClick = {
                            runAction(OnSearchQueryChangeAction(query = ""))
                        },
                        placeholder = "Search this shelf…",
                    )
                }

                if (isReadTab && availableReadYears.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    ReadYearChipRow(
                        years = availableReadYears,
                        selectedYear = state.selectedReadYear,
                        onYearClick = { year ->
                            runAction(OnReadYearSelectedAction(year = year))
                        },
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LibraryControlStrip(
                    state = state,
                    tab = currentTab,
                    runAction = runAction,
                    layoutOptions = DesktopLayoutOptions,
                    layoutLabel = ::desktopLayoutLabel,
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

    if (state.isFilterSheetExpanded && currentTab != null) {
        LibraryFilterSheet(
            filters = state.filtersFor(tabId = currentTab.id),
            options = state.availableFilterOptionsFor(tabId = currentTab.id),
            onToggle = { value ->
                runAction(
                    OnToggleFilterValueAction(
                        tabId = currentTab.id,
                        value = value,
                    ),
                )
            },
            onClearAll = { runAction(OnClearFiltersAction(tabId = currentTab.id)) },
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
 * A long-press routes to the shelf-visibility settings, mirroring the mobile tab affordance.
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
            .padding(vertical = 16.dp),
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
    isSearchActive: Boolean,
    isRefreshing: Boolean,
    onToggleSearchClick: () -> Unit,
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

            DesktopTooltip(text = if (isSearchActive) "Close" else "Search") {
                IconButton(
                    onClick = onToggleSearchClick,
                    modifier = Modifier.pointerHandCursor(),
                ) {
                    val searchToggleIcon = drawableIconResource(
                        icon = if (isSearchActive) SoftcoverIcon.Close else SoftcoverIcon.Search,
                        contentDescription = if (isSearchActive) "Close library search" else "Search in library",
                    )

                    Icon(
                        painter = searchToggleIcon.getIconPainter(),
                        contentDescription = searchToggleIcon.contentDescription,
                    )
                }
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

/**
 * Desktop offers only the two grid *densities* — covers with captions vs covers alone — because the
 * column count is adaptive to the content width ([desktopGridColumns]); the phone-tuned "2 vs 3 per
 * row" and list options don't apply. [desktopLayoutLabel] drops the "N per row" wording for the same
 * reason.
 */
private val DesktopLayoutOptions: List<LibraryGridLayout> = listOf(
    LibraryGridLayout.GRID_TWO_COLUMNS,
    LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
)

private fun desktopLayoutLabel(layout: LibraryGridLayout): String = when (layout) {
    LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
    LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
        -> "Covers only"

    else -> "Covers with details"
}

private val SHELF_SIDEBAR_WIDTH = 208.dp
