package nl.rhaydus.softcover.feature.library.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.presentation.model.LibraryTab as LibraryContentTab
import nl.rhaydus.softcover.core.presentation.util.TabReselectScrollToTopEffect
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnShelvesSheetCloseRequestedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnShelvesSheetDismissedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnShelvesSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnTabSelectedAction
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryShelvesSheet
import nl.rhaydus.softcover.feature.library.presentation.component.ShelfNeighbourRail
import nl.rhaydus.softcover.feature.library.presentation.screen.section.BookList
import nl.rhaydus.softcover.feature.library.presentation.screen.section.EditionList
import nl.rhaydus.softcover.feature.library.presentation.screen.section.LibraryActiveFilterChipsSection
import nl.rhaydus.softcover.feature.library.presentation.screen.section.LibraryEditModeBackHandlers
import nl.rhaydus.softcover.feature.library.presentation.screen.section.LibrarySearchAndControls
import nl.rhaydus.softcover.feature.library.presentation.screen.section.LibrarySelectionHeader
import nl.rhaydus.softcover.feature.library.presentation.screen.section.LibrarySheets
import nl.rhaydus.softcover.feature.library.presentation.screen.section.MastheadHeader
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
    LaunchedEffect(tabs, state.selectedTabId, state.isShelvesSheetPickPending) {
        val targetIndex = tabs.indexOfFirst { it.id == state.selectedTabId }

        if (targetIndex >= 0 && targetIndex != pagerState.currentPage) {
            pagerState.scrollToPage(targetIndex)
        }

        if (state.isShelvesSheetPickPending) {
            // One frame lets the picked shelf compose and draw while the sheet is still static, so
            // the hide animation does not start on that frame.
            withFrameNanos { }

            runAction(OnShelvesSheetCloseRequestedAction())
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

    currentTab?.let { shelf ->
        TabReselectScrollToTopEffect(
            tab = LibraryTab,
            state = gridStateFor(shelf.id),
        )
    }

    val pullToRefreshState = rememberPullToRefreshState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = topAppBarState)

    LibraryEditModeBackHandlers(
        selectionMode = state.selectionMode,
        isRearranging = state.isRearranging,
        runAction = runAction,
    )

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
    ) {
        Column(
            modifier = Modifier.padding(it),
        ) {
            if (state.selectionMode) {
                LibrarySelectionHeader(
                    selectedCount = state.selectionCount,
                    pills = state.selectionPills,
                    isMoveMenuExpanded = state.isBulkMoveMenuExpanded,
                    runAction = runAction,
                )
            } else {
                val masthead = state.mastheadFor(currentTab?.id)

                MastheadHeader(
                    masthead = masthead,
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

                LibrarySearchAndControls(
                    state = state,
                    tab = currentTab,
                    runAction = runAction,
                )

                LibraryActiveFilterChipsSection(
                    tabId = currentTab?.id,
                    chips = currentTab?.id?.let { state.activeFilterChipsFor(tabId = it) },
                    runAction = runAction,
                )

                // Hidden while rearranging because the pager is frozen then too — the rail promises a
                // swipe that mode does not accept. Selection mode never reaches here: this whole
                // branch is the not-selecting header.
                AnimatedVisibility(
                    visible = state.shelfSwipeEnabled && tabs.size > 1 && state.isRearranging.not(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(Spacing.sm))

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

            Spacer(modifier = Modifier.height(Spacing.sm))

            PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = {
                    runAction(OnRefreshAction())
                },
                indicator = {
                    PullToRefreshDefaults.LoadingIndicator(
                        state = pullToRefreshState,
                        isRefreshing = state.isLoading,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
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

    state.shelvesSheet.modelOrNull?.let { shelvesSheet ->
        LibraryShelvesSheet(
            model = shelvesSheet,
            runAction = runAction,
            onDismissRequest = { runAction(OnShelvesSheetDismissedAction()) },
        )
    }

    LibrarySheets(
        state = state,
        currentTabId = currentTab?.id,
        runAction = runAction,
    )
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
            gridStateFor = { LazyGridState() },
            topAppBarState = rememberTopAppBarState(),
        )
    }
}
