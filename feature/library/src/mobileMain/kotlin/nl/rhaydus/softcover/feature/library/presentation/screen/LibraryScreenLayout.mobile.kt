package nl.rhaydus.softcover.feature.library.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.IndicatorBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import nl.rhaydus.designsystem.editorial.component.EditorialSearchField
import nl.rhaydus.designsystem.editorial.component.PullToRefreshEyebrow
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.component.ChooseListsBottomSheet
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab as LibraryContentTab
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
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
import nl.rhaydus.softcover.feature.library.presentation.action.OnShelvesSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnTabSelectedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnToggleFilterValueAction
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryArrangeSheet
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryControlLine
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryFilterChipRow
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryFilterSheet
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryShelvesSheet
import nl.rhaydus.softcover.feature.library.presentation.component.ShelfNeighbourRail
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalComposeUiApi::class,
)
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

    val scope = rememberCoroutineScope()

    // Re-keyed on tabsLoaded so the pager lands on the persisted shelf the moment VisibleTabsCollector
    // resolves the real tab set — without it the pager would sit on page 0 (All) forever.
    val initialPage = remember(state.tabsLoaded) {
        tabs.indexOfFirst { it.id == state.selectedTabId }.coerceAtLeast(0)
    }

    val pagerState = key(state.tabsLoaded) {
        rememberPagerState(
            initialPage = initialPage,
            pageCount = { tabs.size },
        )
    }

    // Selection made elsewhere (the Shelves sheet, a tab being hidden in settings) snaps the pager
    // across instantly: the sheet is dismissing over it, and animating a multi-shelf jump behind a
    // closing sheet reads as a glitch. The rail animates instead — see onPreviousClick below.
    LaunchedEffect(tabs, state.selectedTabId) {
        val targetIndex = tabs.indexOfFirst { it.id == state.selectedTabId }

        if (targetIndex >= 0 && targetIndex != pagerState.currentPage) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    val currentTabs by rememberUpdatedState(tabs)
    val currentSelectedTabId by rememberUpdatedState(state.selectedTabId)

    // Keyed on pagerState alone, so this collector outlives every tab-list change; the two
    // rememberUpdatedState reads above are what keep it from closing over a stale snapshot.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .drop(1)
            .collect { page ->
                currentTabs.getOrNull(page)?.id?.let { id ->
                    if (id != currentSelectedTabId) {
                        runAction(OnTabSelectedAction(tabId = id))
                    }
                }
            }
    }

    // The header follows the pager rather than the state: currentPage flips as the incoming shelf
    // passes halfway, so title, stats, control line, filter chips and rail all change over with the
    // content. The tab-select action still fires only on settle, so a cancelled swipe writes nothing.
    //
    // That deliberately leaves state trailing the header for the length of a fling, which anything
    // reading selectedTabId sees — OnRefreshAction picks its RefreshScope from it, so a pull-to-refresh
    // landed mid-fling refreshes the outgoing shelf. Accepted: it needs two overlapping gestures inside
    // a ~300ms window and the worst case is refreshing the neighbouring scope. Threading the visible
    // page into the action instead would give "the current tab" a second source of truth, which is the
    // worse trade.
    val currentTabIndex = pagerState.currentPage.coerceAtMost(tabs.lastIndex.coerceAtLeast(0))
    val currentTab = tabs.getOrNull(currentTabIndex)

    val pullToRefreshState = rememberPullToRefreshState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = topAppBarState)

    val haptics = LocalHaptics.current

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

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
    ) {
        Column(
            modifier = Modifier.padding(it),
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
                val currentTabStats = currentTab?.let { tab -> state.tabStatsFor(tabId = tab.id) }

                MastheadHeader(
                    tab = currentTab,
                    bookCount = currentTabStats?.itemCount,
                    totalPages = currentTabStats?.totalPages ?: 0,
                    pullToRefreshState = pullToRefreshState,
                    isRefreshing = state.isLoading,
                    topAppBarState = topAppBarState,
                    onTitleClick = { runAction(OnShelvesSheetExpandedChangeAction(expanded = true)) },
                    onCollapsibleSized = { measured ->
                        val newLimit = -measured.toFloat()

                        if (measured > 0 && newLimit != topAppBarState.heightOffsetLimit) {
                            val previousFraction = if (topAppBarState.heightOffsetLimit < 0f) {
                                (topAppBarState.heightOffset / topAppBarState.heightOffsetLimit)
                                    .coerceIn(
                                        0f,
                                        1f,
                                    )
                            } else {
                                0f
                            }

                            topAppBarState.heightOffsetLimit = newLimit
                            topAppBarState.heightOffset = previousFraction * newLimit
                        }
                    },
                )

                // Search is persistent (no toggle) per the redesign — always rendered outside selection mode.
                Column {
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
                }

                val activeFilters = currentTab?.id?.let { state.filtersFor(tabId = it) }

                AnimatedVisibility(
                    visible = activeFilters != null && activeFilters.isEmpty.not(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    val tabId = currentTab?.id
                    val filters = activeFilters

                    if (tabId != null && filters != null) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))

                            LibraryFilterChipRow(
                                filters = filters,
                                onRemove = { value ->
                                    runAction(
                                        OnToggleFilterValueAction(
                                            tabId = tabId,
                                            value = value,
                                        ),
                                    )
                                },
                                onClearAll = {
                                    runAction(OnClearFiltersAction(tabId = tabId))
                                },
                            )
                        }
                    }
                }

                // Hidden while rearranging because the pager is frozen then too — the rail promises a
                // swipe that mode does not accept. Selection mode never reaches here: this whole
                // branch is the not-selecting header.
                AnimatedVisibility(
                    visible = state.shelfSwipeEnabled && tabs.size > 1 && state.isRearranging.not(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))

                        ShelfNeighbourRail(
                            previousLabel = tabs.getOrNull(currentTabIndex - 1)?.label,
                            nextLabel = tabs.getOrNull(currentTabIndex + 1)?.label,
                            onPreviousClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(currentTabIndex - 1)
                                }
                            },
                            onNextClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(currentTabIndex + 1)
                                }
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = {
                    runAction(OnRefreshAction())
                },
                indicator = {
                    IndicatorBox(
                        modifier = Modifier.align(Alignment.TopCenter),
                        state = pullToRefreshState,
                        isRefreshing = state.isLoading,
                    ) {
                        ContainedLoadingIndicator(modifier = Modifier.align(Alignment.TopCenter))
                    }
                },
                state = pullToRefreshState,
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            ) {
                // Both editing modes freeze the pager: a page swipe would fight the reorderable grid's
                // drag, and carrying a selection across shelves makes the count subtitle lie.
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = state.shelfSwipeEnabled &&
                        state.selectionMode.not() &&
                        state.isRearranging.not(),
                ) { page ->
                    when (val tab = tabs.getOrNull(page)) {
                        is LibraryContentTab.CustomList -> EditionList(
                            tab = tab,
                            state = state,
                            gridState = gridStateFor(tab.id),
                            onEditionClick = onEditionClick,
                            runAction = runAction,
                        )

                        is LibraryContentTab.All,
                        is LibraryContentTab.Status,
                            -> BookList(
                                tab = tab,
                                state = state,
                                gridState = gridStateFor(tab.id),
                                onBookClick = onBookClick,
                                runAction = runAction,
                            )

                        null -> Unit
                    }
                }
            }
        }
    }

    if (state.isShelvesSheetExpanded) {
        LibraryShelvesSheet(
            state = state,
            runAction = runAction,
            onManageClick = {
                runAction(OnShelvesSheetExpandedChangeAction(expanded = false))

                onTabLongPress()
            },
            onDismissRequest = {
                runAction(OnShelvesSheetExpandedChangeAction(expanded = false))
            },
        )
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
            bookCovers = state.resolveSelectedBooks().take(3).mapNotNull { it.currentEdition },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MastheadHeader(
    tab: LibraryContentTab?,
    bookCount: Int?,
    totalPages: Int,
    pullToRefreshState: PullToRefreshState,
    isRefreshing: Boolean,
    topAppBarState: TopAppBarState,
    onTitleClick: () -> Unit,
    onCollapsibleSized: (Int) -> Unit,
) {
    // Reads heightOffset only inside graphicsLayer/layout lambdas so a per-frame offset
    // change re-runs only those draw/layout phases — never a recomposition of the whole header.
    val collapseFraction: () -> Float = {
        val limit = topAppBarState.heightOffsetLimit

        if (limit < 0f) (topAppBarState.heightOffset / limit).coerceIn(
            0f,
            1f,
        ) else 0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 8.dp, top = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PullToRefreshEyebrow(
                pullToRefreshState = pullToRefreshState,
                isRefreshing = isRefreshing,
                baseText = "Your collection",
                refreshingText = "Refreshing your shelf…",
                modifier = Modifier.weight(1f),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = 1f - collapseFraction() }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    onCollapsibleSized(placeable.height)
                    val visibleHeight = (placeable.height * (1f - collapseFraction()))
                        .toInt()
                        .coerceAtLeast(0)
                    layout(placeable.width, visibleHeight) {
                        placeable.place(
                            0,
                            0,
                        )
                    }
                },
        ) {
            Column {
                Spacer(modifier = Modifier.height(4.dp))

                // The masthead title IS the shelf switcher (redesign brief): tapping it opens the
                // Shelves sheet, replacing the retired pill tab row.
                Row(
                    modifier = Modifier
                        .pointerHandCursor()
                        .pressScaleClickable(onClick = onTitleClick)
                        .padding(end = 16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = tab?.label ?: "Library",
                        style = MaterialTheme.editorialTypography.pageTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        autoSize = TextAutoSize.StepBased(
                            maxFontSize = MaterialTheme.editorialTypography.pageTitle.fontSize,
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(modifier = Modifier.width(9.dp))

                    val chevronIcon = drawableIconResource(
                        icon = SoftcoverIcon.ArrowDropDown,
                        contentDescription = "Switch shelf",
                    )

                    Icon(
                        painter = chevronIcon.getIconPainter(),
                        contentDescription = chevronIcon.contentDescription,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 7.dp)
                            .size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val subtitle = subtitleFor(
                    tab = tab,
                    bookCount = bookCount,
                    totalPages = totalPages,
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.editorialTypography.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 16.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@StandardPreview
@Composable
private fun LibraryScreenPreview() {
    SoftcoverTheme {
        LibraryScreenLayout(
            state = LibraryUiState(
                visibleTabs = listOf(
                    LibraryContentTab.All,
                    LibraryContentTab.Status.of(UserBookStatus.CURRENTLY_READING),
                    LibraryContentTab.Status.of(UserBookStatus.WANT_TO_READ),
                ),
                booksByTab = mapOf(
                    "status-1" to listOf(
                        PreviewData.baseBook.copy(title = "Last to Leave the room"),
                        PreviewData.baseBook.copy(title = "Futility"),
                        PreviewData.baseBook.copy(title = "We call them witches"),
                    ),
                ),
            ),
            runAction = {},
            onBookClick = {},
            onEditionClick = {},
            onTabLongPress = {},
            onCreateNewListClick = {},
            gridStateFor = { LazyGridState() },
            topAppBarState = rememberTopAppBarState(),
        )
    }
}

@StandardPreview
@Composable
private fun LibraryEmptyScreenPreview() {
    SoftcoverTheme {
        LibraryScreenLayout(
            state = LibraryUiState(
                visibleTabs = listOf(
                    LibraryContentTab.All,
                    LibraryContentTab.Status.of(UserBookStatus.WANT_TO_READ),
                ),
                booksByTab = mapOf("status-1" to emptyList()),
                isLoading = false,
            ),
            runAction = {},
            onBookClick = {},
            onEditionClick = {},
            onTabLongPress = {},
            onCreateNewListClick = {},
            gridStateFor = { LazyGridState() },
            topAppBarState = rememberTopAppBarState(),
        )
    }
}
