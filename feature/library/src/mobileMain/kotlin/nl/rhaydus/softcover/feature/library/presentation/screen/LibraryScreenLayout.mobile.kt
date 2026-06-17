package nl.rhaydus.softcover.feature.library.presentation.screen

import nl.rhaydus.designsystem.editorial.component.EditorialSearchField
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import nl.rhaydus.designsystem.editorial.component.PullToRefreshEyebrow
import nl.rhaydus.designsystem.haptics.LocalHaptics
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

    val pullToRefreshState = rememberPullToRefreshState()

    val density = LocalDensity.current
    val containerSize = LocalWindowInfo.current.containerSize
    val screenWidthDp = with(density) { containerSize.width.toDp() }
    val maxTabLabelWidth = (screenWidthDp - 168.dp).coerceAtLeast(120.dp)

    val initialPage = remember(state.tabsLoaded) {
        tabs.indexOfFirst { it.id == state.selectedTabId }.coerceAtLeast(0)
    }

    val pagerState = key(state.tabsLoaded) {
        rememberPagerState(
            initialPage = initialPage,
            pageCount = { tabs.size },
        )
    }

    LaunchedEffect(tabs, state.selectedTabId) {
        val targetIndex = tabs.indexOfFirst { it.id == state.selectedTabId }

        if (targetIndex >= 0 && targetIndex != pagerState.currentPage) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    val currentTabs by rememberUpdatedState(tabs)
    val currentSelectedTabId by rememberUpdatedState(state.selectedTabId)

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

    val currentTabIndex = pagerState.currentPage.coerceAtMost(tabs.lastIndex.coerceAtLeast(0))
    val currentTab = tabs.getOrNull(currentTabIndex)
    val isReadTab = currentTab is LibraryContentTab.Status &&
            currentTab.status == UserBookStatus.READ

    // Books are pre-sorted by the DAO (SQL ORDER BY), so this is just a filter pass.
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

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = topAppBarState)

    val collapseFraction = if (topAppBarState.heightOffsetLimit < 0f) {
        (topAppBarState.heightOffset / topAppBarState.heightOffsetLimit).coerceIn(
            0f,
            1f,
        )
    } else {
        0f
    }

    val haptics = LocalHaptics.current

    BackHandler(enabled = state.selectionMode) {
        runAction(OnExitSelectionModeAction())
    }

    BackHandler(enabled = state.isRearranging) {
        runAction(OnExitRearrangeModeAction())
    }

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
                EditorialHeader(
                    tabLabel = currentTab?.label,
                    bookCount = currentTabBookCount,
                    totalPages = currentTabPageCount,
                    tab = currentTab,
                    isSearchActive = state.isSearchActive,
                    onToggleSearchClick = { runAction(OnToggleSearchAction()) },
                    pullToRefreshState = pullToRefreshState,
                    isRefreshing = state.isLoading,
                    collapseFraction = collapseFraction,
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
            }

            AnimatedVisibility(
                visible = state.isSearchActive && state.selectionMode.not(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
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
                        placeholder = "Search this shelf…",
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            AnimatedVisibility(
                visible = state.selectionMode.not(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                ShelfTabRow(
                    tabs = tabs,
                    currentPage = currentTabIndex,
                    maxLabelWidth = maxTabLabelWidth,
                    onTabClick = { index ->
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    onTabLongPress = onTabLongPress,
                )
            }

            AnimatedVisibility(
                visible = isReadTab && availableReadYears.isNotEmpty() && state.selectionMode.not(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    ReadYearChipRow(
                        years = availableReadYears,
                        selectedYear = state.selectedReadYear,
                        onYearClick = { year ->
                            runAction(OnReadYearSelectedAction(year = year))
                        },
                    )
                }
            }

            AnimatedVisibility(
                visible = state.selectionMode.not(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    LibraryControlStrip(
                        state = state,
                        tab = currentTab,
                        runAction = runAction,
                    )
                }
            }

            val activeFilters = currentTab?.id?.let { state.filtersFor(tabId = it) }

            AnimatedVisibility(
                visible = activeFilters != null && activeFilters.isEmpty.not() && state.selectionMode.not(),
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
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = state.selectionMode.not(),
                ) { page ->
                    val tab = tabs.getOrNull(page) ?: return@HorizontalPager

                    when (tab) {
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
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorialHeader(
    tabLabel: String?,
    bookCount: Int?,
    totalPages: Int,
    tab: LibraryContentTab?,
    isSearchActive: Boolean,
    onToggleSearchClick: () -> Unit,
    pullToRefreshState: PullToRefreshState,
    isRefreshing: Boolean,
    collapseFraction: Float,
    onCollapsibleSized: (Int) -> Unit,
) {
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

            IconButton(onClick = onToggleSearchClick) {
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = 1f - collapseFraction }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    onCollapsibleSized(placeable.height)
                    val visibleHeight = (placeable.height * (1f - collapseFraction))
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

                Text(
                    text = tabLabel ?: "Library",
                    style = MaterialTheme.editorialTypography.pageTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 16.dp),
                )

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
