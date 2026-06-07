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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
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
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import nl.rhaydus.softcover.core.designsystem.presentation.component.ChooseListsBottomSheet
import nl.rhaydus.softcover.core.designsystem.presentation.component.DeadlineBadge
import nl.rhaydus.softcover.core.designsystem.presentation.component.DeadlineCoverOverlay
import nl.rhaydus.softcover.core.designsystem.presentation.component.DeadlineSummaryLine
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.component.PullToRefreshEyebrow
import nl.rhaydus.softcover.core.designsystem.presentation.component.rememberLazyItemMutationAnimator
import nl.rhaydus.softcover.core.designsystem.presentation.component.mutationAnimated
import nl.rhaydus.softcover.core.designsystem.presentation.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.softcover.core.designsystem.presentation.component.staggeredEntry
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab as LibraryContentTab
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.pressScaleCombinedClickable
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.prefetchBookDetailOnPress
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.rememberBookDetailPrefetcher
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.designsystem.presentation.util.LocalHaptics
import nl.rhaydus.softcover.core.designsystem.presentation.util.rememberBottomBarPadding
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkAddToListSheetShownAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkMoveMenuExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkMoveShelfAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveDialogExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveFromLibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkToggleListMembershipAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnClearFiltersAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnEnterSelectionModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnExitRearrangeModeAction
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

object LibraryScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val appNavigator = koinInject<AppNavigator>()

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
                        item = appNavigator.screen(
                            ScreenDestination.BookDetail(
                                id = it.id,
                                initialCover = BookInitialCover.fromBook(book = it),
                            ),
                        ),
                    )
                },
                onEditionClick = {
                    navigator.parent?.push(
                        item = appNavigator.screen(
                            ScreenDestination.BookDetail(
                                id = it.bookId,
                                initialCover = BookInitialCover.fromEdition(edition = it),
                                transitionSurface = "edition-${it.id}",
                            ),
                        ),
                    )
                },
                onTabLongPress = {
                    navigator.parent?.push(
                        item = appNavigator.screen(ScreenDestination.LibraryVisibilitySettings),
                    )
                },
                onCreateNewListClick = {
                    screenModel.runAction(OnBulkAddToListSheetShownAction(shown = false))

                    navigator.parent?.push(item = appNavigator.screen(ScreenDestination.CreateList))
                },
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    internal fun Screen(
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
                    val searchToggleIcon = SoftcoverIconResource.Drawable(
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
                val searchIcon = SoftcoverIconResource.Drawable(
                    icon = SoftcoverIcon.Search,
                    contentDescription = "Search",
                )

                Icon(
                    painter = searchIcon.getIconPainter(),
                    contentDescription = searchIcon.contentDescription,
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
                            ),
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
                        val clearSearchIcon = SoftcoverIconResource.Drawable(
                            icon = SoftcoverIcon.Close,
                            contentDescription = "Clear search",
                        )

                        Icon(
                            painter = clearSearchIcon.getIconPainter(),
                            contentDescription = clearSearchIcon.contentDescription,
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

                    LayoutBookEntry(
                        modifier = Modifier.mutationAnimated(
                            scope = gridItemScope,
                            animator = animator,
                            itemKey = book.id,
                        )
                            .staggeredEntry(
                                coordinator = entry,
                                index = index,
                            ),
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
            modifier = modifier.pressScaleCombinedClickable(
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

                if (trailing != null) {
                    Spacer(modifier = Modifier.width(8.dp))

                    trailing()
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
                    ),
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
