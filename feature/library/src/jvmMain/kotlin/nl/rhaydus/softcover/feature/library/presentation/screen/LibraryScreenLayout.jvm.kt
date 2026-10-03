package nl.rhaydus.softcover.feature.library.presentation.screen

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.editorial.component.EditorialSearchField
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.modifier.dismissOnEscape
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.core.component.header.MastheadRefreshAction
import nl.rhaydus.softcover.core.component.header.MastheadRefreshEvent
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.component.lists.ChooseListsBottomSheet
import nl.rhaydus.softcover.core.component.lists.ChooseListsEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sidebar.Sidebar
import nl.rhaydus.softcover.core.component.sidebar.SidebarEvent
import nl.rhaydus.softcover.core.component.sidebar.SidebarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.presentation.model.LibraryTab as LibraryContentTab
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnActiveFilterChipClickedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnArrangeSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkAddToListSheetShownAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkMoveMenuExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkMoveShelfAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveFromLibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkToggleListMembershipAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnExitRearrangeModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnExitSelectionModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnFilterSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSearchQueryChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnTabSelectedAction
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryArrangeSheet
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryControlLine
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryFilterChipRow
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryFilterSheet
import nl.rhaydus.softcover.feature.library.presentation.screen.section.BookList
import nl.rhaydus.softcover.feature.library.presentation.screen.section.EditionList
import nl.rhaydus.softcover.feature.library.presentation.screen.section.SelectionHeader
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

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
            sidebar = state.shelfSidebar,
            onTabClick = { id -> runAction(OnTabSelectedAction(tabId = id)) },
            onTabLongPress = onTabLongPress,
            modifier = Modifier
                .width(208.dp)
                .fillMaxHeight(),
        )

        Divider(model = DividerUiModel.Vertical())

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
                        runAction(OnBulkRemoveSheetExpandedChangeAction(shown = true))
                    },
                )
            } else {
                PageMasthead(
                    model = state.mastheadFor(currentTab?.id),
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 12.dp,
                        top = 16.dp,
                    ),
                    isLoading = state.isMastheadLoadingFor(currentTab?.id),
                ) {
                    MastheadRefreshAction(
                        model = state.mastheadRefresh,
                        onEvent = { event ->
                            when (event) {
                                MastheadRefreshEvent.Refresh -> runAction(OnRefreshAction())
                            }
                        },
                    )
                }

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

                val activeFilterChips = currentTab?.id?.let { state.activeFilterChipsFor(tabId = it) }

                if (currentTab != null && activeFilterChips != null && activeFilterChips.chips.isEmpty.not()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    LibraryFilterChipRow(
                        activeFilters = activeFilterChips,
                        onChipEvent = { event ->
                            when (event) {
                                is ChipEvent.Clicked ->
                                    runAction(OnActiveFilterChipClickedAction(
                                        tabId = currentTab.id,
                                        key = event.key,
                                    ),)

                                is ChipEvent.Dismissed -> Unit
                                is ChipEvent.SpoilerToggled -> Unit
                            }
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

    if (state.isBulkRemoveSheetShown) {
        SheetScaffold(
            model = state.bulkRemoveSheet,
            onEvent = { event ->
                when (event) {
                    SheetScaffoldEvent.PrimaryClicked -> {
                        haptics.commit()

                        runAction(OnBulkRemoveFromLibraryAction())
                    }

                    SheetScaffoldEvent.Dismissed -> runAction(OnBulkRemoveSheetExpandedChangeAction(shown = false))

                    SheetScaffoldEvent.SecondaryClicked,
                    SheetScaffoldEvent.HeaderActionClicked -> Unit
                }
            },
            content = {},
        )
    }

    val chooseListsSheet = state.chooseListsSheet

    if (state.isBulkAddToListSheetShown && state.selectedBookIds.isNotEmpty() && chooseListsSheet != null) {
        ChooseListsBottomSheet(
            model = chooseListsSheet,
            onEvent = { event ->
                when (event) {
                    is ChooseListsEvent.MembershipToggled -> {
                        haptics.commit()

                        runAction(
                            OnBulkToggleListMembershipAction(
                                listId = event.listId,
                                currentMembership = event.membership,
                            ),
                        )
                    }

                    ChooseListsEvent.NewListRequested -> onCreateNewListClick()

                    ChooseListsEvent.Dismissed -> runAction(OnBulkAddToListSheetShownAction(shown = false))
                }
            },
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
    sidebar: SidebarUiModel<String>,
    onTabClick: (String) -> Unit,
    onTabLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Sidebar(
        model = sidebar,
        onEvent = { event ->
            when (event) {
                is SidebarEvent.Clicked -> onTabClick(event.key)
                is SidebarEvent.LongPressed -> onTabLongPress()
            }
        },
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                top = 16.dp,
                bottom = 16.dp + rememberBottomBarPadding(),
            ),
    )
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
