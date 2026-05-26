package nl.rhaydus.softcover.feature.library.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.IndicatorBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import nl.rhaydus.softcover.R
import nl.rhaydus.softcover.core.PreviewData
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.component.DeadlineBadge
import nl.rhaydus.softcover.core.presentation.component.DeadlineCoverOverlay
import nl.rhaydus.softcover.core.presentation.component.DeadlineSummaryLine
import nl.rhaydus.softcover.core.presentation.component.EditionImage
import nl.rhaydus.softcover.core.presentation.component.PullToRefreshEyebrow
import nl.rhaydus.softcover.core.presentation.component.StaggeredEntryCoordinator
import nl.rhaydus.softcover.core.presentation.component.rememberLazyItemMutationAnimator
import nl.rhaydus.softcover.core.presentation.component.rememberMutationAnimatedModifier
import nl.rhaydus.softcover.core.presentation.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.softcover.core.presentation.component.staggeredEntry
import nl.rhaydus.softcover.core.presentation.modifier.pressScaleCombinedClickable
import nl.rhaydus.softcover.core.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.presentation.util.LocalHaptics
import nl.rhaydus.softcover.core.presentation.util.rememberBottomBarPadding
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.BookDetailScreen
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookInitialCover
import nl.rhaydus.softcover.feature.books.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.feature.books.presentation.prefetch.prefetchBookDetailOnPress
import nl.rhaydus.softcover.feature.books.presentation.prefetch.rememberBookDetailPrefetcher
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import nl.rhaydus.softcover.feature.deadlines.domain.model.DeadlineProgress
import nl.rhaydus.softcover.feature.deadlines.domain.model.DeadlineUnit
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkAddToListSheetShownAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkMoveMenuExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkMoveShelfAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveDialogExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveFromLibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkToggleListMembershipAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnClearFiltersAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnEnterSelectionModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnExitSelectionModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnFilterSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnReadYearSelectedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnReorderListBooksAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnReorderShelfBooksAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSearchQueryChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnTabSelectedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnToggleBookSelectionAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnToggleFilterValueAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnToggleSearchAction
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryControlStrip
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryFilterChipRow
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryFilterSheet
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryScreenScreenModel
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.util.formatBookCount
import nl.rhaydus.softcover.feature.library.presentation.util.formatPageCount
import nl.rhaydus.softcover.feature.library.presentation.util.totalPages
import nl.rhaydus.softcover.feature.lists.presentation.component.ChooseListsBottomSheet
import nl.rhaydus.softcover.feature.lists.presentation.screen.CreateListScreen
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
import nl.rhaydus.softcover.feature.settings.domain.model.SortDirection
import nl.rhaydus.softcover.feature.settings.presentation.screen.LibraryVisibilitySettingsScreen
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryTab as LibraryContentTab

object LibraryScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<LibraryScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()
        val localState by screenModel.localState.collectAsStateWithLifecycle()

        val prefetcher = rememberBookDetailPrefetcher()

        CompositionLocalProvider(LocalBookDetailPrefetcher provides prefetcher) {
            Screen(
                state = state,
                runAction = screenModel::runAction,
                gridStateFor = { id -> localState.gridStates[id] ?: LazyGridState() },
                topAppBarState = screenModel.headerScrollState,
                onBookClick = {
                    navigator.parent?.push(
                        item = BookDetailScreen(
                            id = it.id,
                            initialCover = BookInitialCover.fromBook(book = it),
                        ),
                    )
                },
                onEditionClick = {
                    navigator.parent?.push(
                        item = BookDetailScreen(
                            id = it.bookId,
                            initialCover = BookInitialCover.fromEdition(edition = it),
                            transitionSurface = "edition-${it.id}",
                        ),
                    )
                },
                onTabLongPress = {
                    navigator.parent?.push(item = LibraryVisibilitySettingsScreen())
                },
                onCreateNewListClick = {
                    screenModel.runAction(OnBulkAddToListSheetShownAction(shown = false))

                    navigator.parent?.push(item = CreateListScreen())
                },
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun Screen(
        state: LibraryUiState,
        runAction: (LibraryAction) -> Unit,
        onBookClick: (Book) -> Unit,
        onEditionClick: (BookEdition) -> Unit,
        onTabLongPress: () -> Unit = {},
        onCreateNewListClick: () -> Unit = {},
        gridStateFor: (String) -> LazyGridState = { LazyGridState() },
        topAppBarState: TopAppBarState = rememberTopAppBarState(),
    ) {
        val tabs = state.visibleTabs
        val scope = rememberCoroutineScope()

        val pullToRefreshState = rememberPullToRefreshState()

        val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
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
            (topAppBarState.heightOffset / topAppBarState.heightOffsetLimit).coerceIn(0f, 1f)
        } else {
            0f
        }

        val haptics = LocalHaptics.current

        BackHandler(enabled = state.selectionMode) {
            runAction(OnExitSelectionModeAction())
        }

        Scaffold(
            contentWindowInsets = WindowInsets.statusBars,
        ) {
            Column(
                modifier = Modifier.padding(it)
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
                                        .coerceIn(0f, 1f)
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
                        )

                        Spacer(modifier = Modifier.height(4.dp))
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

    // region Editorial header

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
                    Icon(
                        painter = painterResource(
                            if (isSearchActive) R.drawable.ic_close else R.drawable.ic_search
                        ),
                        contentDescription = if (isSearchActive) "Close library search" else "Search in library",
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
                            placeable.place(0, 0)
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

    // endregion

    // region Search field

    @Composable
    private fun EditorialSearchField(
        query: String,
        onQueryChange: (String) -> Unit,
        onClearClick: () -> Unit,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(28.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search this shelf…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = LocalTextStyle.current.merge(
                            MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (query.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onClearClick,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }

    // endregion

    // region Tabs

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ShelfTabRow(
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

        Surface(
            modifier = modifier.combinedClickable(
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
            shape = RoundedCornerShape(percent = 50),
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

    // endregion

    @Composable
    private fun ReadYearChipRow(
        years: List<Int>,
        selectedYear: Int?,
        onYearClick: (Int?) -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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

    private fun subtitleFor(
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

    @Composable
    private fun EditionList(
        tab: LibraryContentTab.CustomList,
        state: LibraryUiState,
        gridState: LazyGridState,
        onEditionClick: (BookEdition) -> Unit,
        runAction: (LibraryAction) -> Unit,
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

        val isRanked = state.customLists.firstOrNull { it.id == tab.listId }?.ranked == true

        if (sortMode == LibrarySortMode.ORDER && isRanked) {
            ReorderableEditionGrid(
                tab = tab,
                visibleEditions = visibleEditions,
                visibleEditionIds = visibleEditionIds,
                state = state,
                gridState = gridState,
                entry = entry,
                onEditionClick = onEditionClick,
                runAction = runAction,
            )

            return
        }

        LayoutGrid(
            layout = state.gridLayout,
            gridState = gridState,
        ) {
            itemsIndexed(visibleEditions, key = { _, edition -> edition.id }) { index, edition ->
                LayoutEditionEntry(
                    modifier = rememberMutationAnimatedModifier(animator = animator, itemKey = edition.id)
                        .staggeredEntry(coordinator = entry, index = index),
                    edition = edition,
                    layout = state.gridLayout,
                    onEditionClick = onEditionClick,
                )
            }
        }
    }

    /**
     * ORDER-sort path for custom lists with `ranked = true`. Maintains a local shadow of edition
     * ids that the reorder library updates during drag so the grid reflects the move immediately;
     * on drop we map the touched `[min, max]` visible range to `list_books.listBookId`s and
     * dispatch the action. The DAO re-emission becomes the source of truth on the next list
     * refresh.
     *
     * Unlike the built-in shelf path (which writes a prefix `0..maxTouched`), Hardcover's web
     * client rewrites only the contiguous `[minTouched, maxTouched]` range — confirmed by the
     * sample mutations users emit when reordering. We mirror that exactly so two clients editing
     * the same list see consistent server-side semantics.
     */
    @Composable
    private fun ReorderableEditionGrid(
        tab: LibraryContentTab.CustomList,
        visibleEditions: List<BookEdition>,
        visibleEditionIds: List<Int>,
        state: LibraryUiState,
        gridState: LazyGridState,
        entry: StaggeredEntryCoordinator,
        onEditionClick: (BookEdition) -> Unit,
        runAction: (LibraryAction) -> Unit,
    ) {
        val haptics = LocalHaptics.current

        val orderedIds = remember { mutableStateListOf<Int>() }

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

        val reorderableState = rememberReorderableLazyGridState(lazyGridState = gridState) { from, to ->
            val fromIndex = orderedIds.indexOf(from.key as Int)
            val toIndex = orderedIds.indexOf(to.key as Int)

            if (fromIndex == -1 || toIndex == -1) return@rememberReorderableLazyGridState

            orderedIds.add(toIndex, orderedIds.removeAt(fromIndex))

            val current = minTouchedIndex.intValue

            minTouchedIndex.intValue = if (current == -1) {
                minOf(fromIndex, toIndex)
            } else {
                minOf(current, fromIndex, toIndex)
            }

            maxTouchedIndex.intValue = maxOf(maxTouchedIndex.intValue, fromIndex, toIndex)
        }

        LayoutGrid(
            layout = state.gridLayout,
            gridState = gridState,
        ) {
            itemsIndexed(orderedIds, key = { _, id -> id }) { index, id ->
                val edition = editionsById[id] ?: return@itemsIndexed

                ReorderableItem(state = reorderableState, key = id) {
                    Box {
                        LayoutEditionEntry(
                            modifier = Modifier.staggeredEntry(coordinator = entry, index = index),
                            edition = edition,
                            layout = state.gridLayout,
                            onEditionClick = onEditionClick,
                        )

                        DragHandle(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .draggableHandle(
                                    onDragStarted = {
                                        minTouchedIndex.intValue = -1
                                        maxTouchedIndex.intValue = -1

                                        haptics.lift()
                                    },
                                    onDragStopped = {
                                        haptics.drop()

                                        val min = minTouchedIndex.intValue
                                        val max = maxTouchedIndex.intValue

                                        if (min < 0 || max < 0 || max >= orderedIds.size) {
                                            return@draggableHandle
                                        }

                                        val orderedListBookIds = orderedIds
                                            .subList(min, max + 1)
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
                                ),
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun BookList(
        tab: LibraryContentTab,
        state: LibraryUiState,
        gridState: LazyGridState,
        onBookClick: (Book) -> Unit,
        runAction: (LibraryAction) -> Unit,
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

        val manualReorderStatus: UserBookStatus? = if (
            sortMode == LibrarySortMode.MANUAL &&
            tab is LibraryContentTab.Status &&
            tab.status != UserBookStatus.DID_NOT_FINISH &&
            selectionMode.not()
        ) {
            tab.status
        } else {
            null
        }

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

        if (manualReorderStatus != null) {
            ReorderableBookGrid(
                status = manualReorderStatus,
                visibleBooks = visibleBooks,
                visibleBookIds = visibleBookIds,
                state = state,
                gridState = gridState,
                entry = entry,
                onBookClick = onBookClick,
                runAction = runAction,
            )

            return
        }

        val haptics = LocalHaptics.current

        LayoutGrid(
            layout = state.gridLayout,
            gridState = gridState,
        ) {
            itemsIndexed(visibleBooks, key = { _, book -> book.id }) { index, book ->
                val isSelected = selectionMode && book.id in state.selectedBookIds

                val onClick: () -> Unit = if (selectionMode) {
                    {
                        haptics.select()

                        runAction(OnToggleBookSelectionAction(bookId = book.id))
                    }
                } else {
                    { onBookClick(book) }
                }

                val onLongClick: (() -> Unit)? = if (selectionMode) {
                    null
                } else {
                    {
                        haptics.threshold()

                        runAction(OnEnterSelectionModeAction(bookId = book.id))
                    }
                }

                LayoutBookEntry(
                    modifier = rememberMutationAnimatedModifier(animator = animator, itemKey = book.id)
                        .staggeredEntry(coordinator = entry, index = index),
                    book = book,
                    layout = state.gridLayout,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    isSelectionMode = selectionMode,
                    isSelected = isSelected,
                    deadline = state.deadlines[book.id],
                    dateStyle = state.dateStyle,
                )
            }
        }
    }

    /**
     * MANUAL-sort path. Maintains a local shadow of book ids that the reorder library updates
     * during drag so the grid reflects the move immediately; on drop we dispatch the action and
     * the DAO re-emission becomes the source of truth. The shadow re-syncs whenever the canonical
     * (DB-sorted) list changes — e.g. a book is shelved or unshelved from elsewhere.
     *
     * Long-press still enters bulk-select (parent gating then re-routes to the non-MANUAL path so
     * drag handles disappear while a selection is active). Drag is initiated only from the
     * dedicated handle overlaid on each card.
     */
    @Composable
    private fun ReorderableBookGrid(
        status: UserBookStatus,
        visibleBooks: List<Book>,
        visibleBookIds: List<Int>,
        state: LibraryUiState,
        gridState: LazyGridState,
        entry: StaggeredEntryCoordinator,
        onBookClick: (Book) -> Unit,
        runAction: (LibraryAction) -> Unit,
    ) {
        val haptics = LocalHaptics.current

        val orderedIds = remember { mutableStateListOf<Int>() }

        LaunchedEffect(visibleBookIds) {
            if (orderedIds.toList() != visibleBookIds) {
                orderedIds.clear()
                orderedIds.addAll(visibleBookIds)
            }
        }

        val booksById = remember(visibleBooks) { visibleBooks.associateBy { it.id } }

        // Highest visual index touched during the current drag — defines the prefix the user is
        // re-arranging. Reset on drag start, read on drop. Books beyond this index are NOT
        // persisted, so a shallow drag at the top of the shelf leaves the rest of the shelf in
        // its natural order (and newcomers from the API still slot in just below the prefix).
        val maxTouchedIndex = remember { mutableIntStateOf(-1) }

        val reorderableState = rememberReorderableLazyGridState(lazyGridState = gridState) { from, to ->
            val fromIndex = orderedIds.indexOf(from.key as Int)
            val toIndex = orderedIds.indexOf(to.key as Int)

            if (fromIndex == -1 || toIndex == -1) return@rememberReorderableLazyGridState

            orderedIds.add(toIndex, orderedIds.removeAt(fromIndex))

            maxTouchedIndex.intValue = maxOf(maxTouchedIndex.intValue, fromIndex, toIndex)
        }

        LayoutGrid(
            layout = state.gridLayout,
            gridState = gridState,
        ) {
            itemsIndexed(orderedIds, key = { _, id -> id }) { index, id ->
                val book = booksById[id] ?: return@itemsIndexed

                ReorderableItem(state = reorderableState, key = id) {
                    Box {
                        LayoutBookEntry(
                            modifier = Modifier.staggeredEntry(coordinator = entry, index = index),
                            book = book,
                            layout = state.gridLayout,
                            onClick = { onBookClick(book) },
                            onLongClick = {
                                haptics.threshold()

                                runAction(OnEnterSelectionModeAction(bookId = book.id))
                            },
                            isSelectionMode = false,
                            isSelected = false,
                            deadline = state.deadlines[book.id],
                            dateStyle = state.dateStyle,
                        )

                        DragHandle(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .draggableHandle(
                                    onDragStarted = {
                                        maxTouchedIndex.intValue = -1

                                        haptics.lift()
                                    },
                                    onDragStopped = {
                                        haptics.drop()

                                        val touchedDepth = maxTouchedIndex.intValue

                                        if (touchedDepth >= 0 && touchedDepth < orderedIds.size) {
                                            runAction(
                                                OnReorderShelfBooksAction(
                                                    status = status,
                                                    prefixOrderedBookIds = orderedIds
                                                        .take(touchedDepth + 1),
                                                ),
                                            )
                                        }
                                    },
                                ),
                        )
                    }
                }
            }
        }
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
            Icon(
                painter = painterResource(R.drawable.ic_drag_handle),
                contentDescription = "Drag to reorder",
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
            val current = Triple(sortMode, sortDirection, filters)
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
        content: LazyGridScope.() -> Unit,
    ) {
        val columns = when (layout) {
            LibraryGridLayout.GRID_TWO_COLUMNS,
            LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
                -> 2

            LibraryGridLayout.GRID_THREE_COLUMNS,
            LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
                -> 3

            LibraryGridLayout.LIST_COMPACT,
            LibraryGridLayout.LIST_LARGE,
                -> 1
        }

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
            columns = GridCells.Fixed(columns),
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
        deadline: BookDeadline? = null,
        dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
        modifier: Modifier = Modifier,
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

            LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
            LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
                -> {
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

    @Composable
    private fun LayoutEditionEntry(
        edition: BookEdition,
        layout: LibraryGridLayout,
        onEditionClick: (BookEdition) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val entryModifier = modifier.prefetchBookDetailOnPress(edition.bookId)

        val title = edition.title.orEmpty()
        val authorName = edition.authors.map { it.name }.firstOrNull().orEmpty()

        when (layout) {
            LibraryGridLayout.GRID_TWO_COLUMNS,
            LibraryGridLayout.GRID_THREE_COLUMNS,
                -> {
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

            LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
            LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
                -> {
                CoverOnlyCell(
                    modifier = entryModifier,
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

            LibraryGridLayout.LIST_COMPACT -> {
                CompactRow(
                    modifier = entryModifier,
                    title = title,
                    authorName = authorName,
                    onClick = { onEditionClick(edition) },
                )
            }

            LibraryGridLayout.LIST_LARGE -> {
                LargeRow(
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
                .pressScaleCombinedClickable(onClick = onClick, onLongClick = onLongClick)
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
            modifier = modifier.pressScaleCombinedClickable(onClick = onClick, onLongClick = onLongClick)
        ) {
            cover(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio = 2f / 3f)
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
        onLongClick: (() -> Unit)? = null,
        isSelectionMode: Boolean = false,
        isSelected: Boolean = false,
        deadlineProgress: DeadlineProgress? = null,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .pressScaleCombinedClickable(onClick = onClick, onLongClick = onLongClick),
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
        onLongClick: (() -> Unit)? = null,
        seriesText: String? = null,
        releaseYear: Int? = null,
        usersCount: Int? = null,
        rating: Double? = null,
        deadlineProgress: DeadlineProgress? = null,
        dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
        modifier: Modifier = Modifier,
        cover: @Composable (Modifier) -> Unit,
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .pressScaleCombinedClickable(onClick = onClick, onLongClick = onLongClick),
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
                        .aspectRatio(ratio = 2f / 3f)
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

                                Icon(
                                    painter = painterResource(R.drawable.ic_star_filled),
                                    contentDescription = "",
                                    tint = Color(0xFFFBBF23),
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
            }
        }
    }

    // region Selection mode

    @Composable
    private fun SelectionHeader(
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
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Exit selection mode",
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
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bookmark),
                            contentDescription = "Move selected books to another shelf",
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
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bookmark_add),
                        contentDescription = "Add selected books to a list",
                    )
                }

                IconButton(
                    onClick = onRemoveClick,
                    enabled = bulkActionInProgress.not(),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Remove selected books from library",
                    )
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
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                )
            }
        }
    }

    @Composable
    private fun BulkRemoveConfirmationDialog(
        bookCount: Int,
        inProgress: Boolean,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        val titlePlural = if (bookCount == 1) "book" else "books"

        AlertDialog(
            onDismissRequest = {
                if (inProgress.not()) onDismiss()
            },
            title = {
                Text(text = "Remove $bookCount $titlePlural?")
            },
            text = {
                Text(
                    text = "They'll come off every shelf and out of your Hardcover library. " +
                        "You can always add them again later.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    enabled = inProgress.not(),
                ) {
                    Text(text = "Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = inProgress.not(),
                ) {
                    Text(text = "Keep")
                }
            },
        )
    }

    private val SelectionShelfTargets: List<Pair<UserBookStatus, String>> = listOf(
        UserBookStatus.WANT_TO_READ to "Move to Want to Read",
        UserBookStatus.CURRENTLY_READING to "Move to Currently Reading",
        UserBookStatus.READ to "Mark as Read",
    )

    private const val UNSELECTED_COVER_ALPHA = 0.55f

    // endregion

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
}

@StandardPreview
@Composable
private fun LibraryScreenPreview() {
    SoftcoverTheme {
        LibraryScreen.Screen(
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
                    )
                ),
            ),
            onBookClick = {},
            runAction = {},
            onEditionClick = {},
        )
    }
}

@StandardPreview
@Composable
private fun LibraryEmptyScreenPreview() {
    SoftcoverTheme {
        LibraryScreen.Screen(
            state = LibraryUiState(
                visibleTabs = listOf(
                    LibraryContentTab.All,
                    LibraryContentTab.Status.of(UserBookStatus.WANT_TO_READ),
                ),
                booksByTab = mapOf("status-1" to emptyList()),
                isLoading = false,
            ),
            onBookClick = {},
            runAction = {},
            onEditionClick = {},
        )
    }
}
