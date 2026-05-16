package nl.rhaydus.softcover.feature.library.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.R
import nl.rhaydus.softcover.core.PreviewData
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.presentation.component.DeadlineBadge
import nl.rhaydus.softcover.core.presentation.component.DeadlineCoverOverlay
import nl.rhaydus.softcover.core.presentation.component.DeadlineSummaryLine
import nl.rhaydus.softcover.core.presentation.component.EditionImage
import nl.rhaydus.softcover.core.presentation.component.PullToRefreshEyebrow
import nl.rhaydus.softcover.core.presentation.component.rememberLazyItemMutationAnimator
import nl.rhaydus.softcover.core.presentation.component.rememberMutationAnimatedModifier
import nl.rhaydus.softcover.core.presentation.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.softcover.core.presentation.component.staggeredEntry
import nl.rhaydus.softcover.core.presentation.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.presentation.modifier.shakeOnError
import nl.rhaydus.softcover.core.presentation.util.LocalHaptics
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.presentation.util.rememberBottomBarPadding
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.BookDetailScreen
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookInitialCover
import nl.rhaydus.softcover.feature.books.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.feature.books.presentation.prefetch.PrefetchBookDetailOnVisible
import nl.rhaydus.softcover.feature.books.presentation.prefetch.rememberBookDetailPrefetcher
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import nl.rhaydus.softcover.feature.deadlines.domain.model.DeadlineProgress
import nl.rhaydus.softcover.feature.deadlines.domain.model.DeadlineUnit
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnClearSwipeFailureAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnGridLayoutChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnLayoutMenuExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnReadYearSelectedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSearchQueryChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSortMenuExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSortModeChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSwipeMarkAsReadAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSwipeRemoveAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnTabSelectedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnToggleSearchAction
import nl.rhaydus.softcover.feature.library.presentation.component.SwipeRowActions
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryScreenScreenModel
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.settings.presentation.screen.LibraryVisibilitySettingsScreen
import nl.rhaydus.softcover.feature.library.presentation.util.formatBookCount
import nl.rhaydus.softcover.feature.library.presentation.util.formatPageCount
import nl.rhaydus.softcover.feature.library.presentation.util.totalPages
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
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
                        ),
                    )
                },
                onTabLongPress = {
                    navigator.parent?.push(item = LibraryVisibilitySettingsScreen())
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
        gridStateFor: (String) -> LazyGridState = { LazyGridState() },
        topAppBarState: TopAppBarState = rememberTopAppBarState(),
    ) {
        val tabs = state.visibleTabs
        val scope = rememberCoroutineScope()

        val pullToRefreshState = rememberPullToRefreshState()

        val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
        val maxTabLabelWidth = (screenWidthDp - 168.dp).coerceAtLeast(120.dp)

        val initialPage = remember {
            tabs.indexOfFirst { it.id == state.selectedTabId }.coerceAtLeast(0)
        }

        val pagerState = rememberPagerState(
            initialPage = initialPage,
            pageCount = { tabs.size },
        )

        LaunchedEffect(tabs, state.selectedTabId) {
            val targetIndex = tabs.indexOfFirst { it.id == state.selectedTabId }

            if (targetIndex >= 0 && targetIndex != pagerState.currentPage) {
                pagerState.scrollToPage(targetIndex)
            }
        }

        LaunchedEffect(pagerState, tabs) {
            snapshotFlow { pagerState.settledPage }.collect { page ->
                tabs.getOrNull(page)?.id?.let { id ->
                    if (id != state.selectedTabId) {
                        runAction(OnTabSelectedAction(tabId = id))
                    }
                }
            }
        }

        val currentTabIndex = pagerState.currentPage.coerceAtMost(tabs.lastIndex.coerceAtLeast(0))
        val currentTab = tabs.getOrNull(currentTabIndex)
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

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = topAppBarState)

        val collapseFraction = if (topAppBarState.heightOffsetLimit < 0f) {
            (topAppBarState.heightOffset / topAppBarState.heightOffsetLimit).coerceIn(0f, 1f)
        } else {
            0f
        }

        Scaffold(
            contentWindowInsets = WindowInsets.statusBars,
        ) {
            Column(
                modifier = Modifier.padding(it)
            ) {
                EditorialHeader(
                    tabLabel = currentTab?.label,
                    bookCount = currentTabBookCount,
                    totalPages = currentTabPageCount,
                    tab = currentTab,
                    isSearchActive = state.isSearchActive,
                    onToggleSearchClick = { runAction(OnToggleSearchAction()) },
                    layoutMenu = {
                        SortMenuAction(
                            state = state,
                            tabId = currentTab?.id,
                            runAction = runAction,
                        )

                        LayoutMenuAction(
                            state = state,
                            runAction = runAction,
                        )
                    },
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

                AnimatedVisibility(
                    visible = state.isSearchActive,
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

                AnimatedVisibility(
                    visible = isReadTab && availableReadYears.isNotEmpty(),
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
                    ) { page ->
                        val tab = tabs.getOrNull(page) ?: return@HorizontalPager

                        when (tab) {
                            is LibraryContentTab.CustomList -> EditionList(
                                tab = tab,
                                state = state,
                                gridState = gridStateFor(tab.id),
                                onEditionClick = onEditionClick,
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
        layoutMenu: @Composable () -> Unit,
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

                layoutMenu()
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
    private fun SortMenuAction(
        state: LibraryUiState,
        tabId: String?,
        runAction: (LibraryAction) -> Unit,
    ) {
        val currentTabId = tabId ?: return
        val currentMode = state.sortModeFor(tabId = currentTabId)

        val supportedModes = remember(currentTabId) {
            val isCustomList = currentTabId.startsWith("list-")

            if (isCustomList) {
                listOf(
                    LibrarySortMode.TITLE,
                    LibrarySortMode.AUTHOR,
                    LibrarySortMode.PAGE_COUNT,
                )
            } else {
                LibrarySortMode.entries
            }
        }

        Box {
            IconButton(
                onClick = {
                    runAction(OnSortMenuExpandedChangeAction(expanded = true))
                },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_sort),
                    contentDescription = "Change library sort",
                )
            }

            DropdownMenu(
                expanded = state.isSortMenuExpanded,
                onDismissRequest = {
                    runAction(OnSortMenuExpandedChangeAction(expanded = false))
                },
            ) {
                supportedModes.forEach { mode ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (mode == currentMode) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                            )
                        },
                        onClick = {
                            runAction(
                                OnSortModeChangeAction(
                                    tabId = currentTabId,
                                    mode = mode,
                                ),
                            )
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun LayoutMenuAction(
        state: LibraryUiState,
        runAction: (LibraryAction) -> Unit,
    ) {
        Box {
            IconButton(
                onClick = {
                    runAction(OnLayoutMenuExpandedChangeAction(expanded = true))
                },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_view_layout),
                    contentDescription = "Change library layout",
                )
            }

            DropdownMenu(
                expanded = state.isLayoutMenuExpanded,
                onDismissRequest = {
                    runAction(OnLayoutMenuExpandedChangeAction(expanded = false))
                },
            ) {
                LibraryGridLayout.entries.forEach { layout ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = layout.label,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        onClick = {
                            runAction(OnGridLayoutChangeAction(newLayout = layout))
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun EditionList(
        tab: LibraryContentTab.CustomList,
        state: LibraryUiState,
        gridState: LazyGridState,
        onEditionClick: (BookEdition) -> Unit,
    ) {
        val rawEditions = state.editionsByTab[tab.id] ?: return

        if (rawEditions.isEmpty() && state.isLoading.not()) {
            EmptyListScreen(tab = tab)

            return
        }

        val visibleEditions = state.displayEditionsFor(tabId = tab.id).orEmpty()

        val animator = rememberLazyItemMutationAnimator(keys = visibleEditions.map { it.id })

        val entry = rememberStaggeredEntryCoordinator(key = "library:editions:${tab.id}")

        ScrollToTopOnSortChange(
            tabId = tab.id,
            sortMode = state.sortModeFor(tabId = tab.id),
            gridState = gridState,
        )

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

        val visibleBooks = state.displayBooksFor(tabId = tab.id).orEmpty()

        val animator = rememberLazyItemMutationAnimator(keys = visibleBooks.map { it.id })

        val entry = rememberStaggeredEntryCoordinator(key = "library:books:${tab.id}")

        ScrollToTopOnSortChange(
            tabId = tab.id,
            sortMode = state.sortModeFor(tabId = tab.id),
            gridState = gridState,
        )

        LayoutGrid(
            layout = state.gridLayout,
            gridState = gridState,
        ) {
            itemsIndexed(visibleBooks, key = { _, book -> book.id }) { index, book ->
                LayoutBookEntry(
                    modifier = rememberMutationAnimatedModifier(animator = animator, itemKey = book.id)
                        .staggeredEntry(coordinator = entry, index = index),
                    book = book,
                    layout = state.gridLayout,
                    onBookClick = onBookClick,
                    deadline = state.deadlines[book.id],
                    dateStyle = state.dateStyle,
                    onSwipeMarkAsRead = { runAction(OnSwipeMarkAsReadAction(book = book)) },
                    onSwipeRemove = { runAction(OnSwipeRemoveAction(book = book)) },
                    swipeFailed = book.id in state.failedSwipeBookIds,
                    onSwipeShakeEnd = { runAction(OnClearSwipeFailureAction(bookId = book.id)) },
                )
            }
        }
    }

    @Composable
    private fun ScrollToTopOnSortChange(
        tabId: String,
        sortMode: LibrarySortMode,
        gridState: LazyGridState,
    ) {
        var previousSortMode by remember(tabId) { mutableStateOf<LibrarySortMode?>(null) }

        LaunchedEffect(tabId, sortMode) {
            val prior = previousSortMode

            if (prior != null && prior != sortMode) {
                gridState.scrollToItem(index = 0)
            }

            previousSortMode = sortMode
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
        onBookClick: (Book) -> Unit,
        deadline: BookDeadline? = null,
        dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
        onSwipeMarkAsRead: () -> Unit = {},
        onSwipeRemove: () -> Unit = {},
        swipeFailed: Boolean = false,
        onSwipeShakeEnd: () -> Unit = {},
        modifier: Modifier = Modifier,
    ) {
        PrefetchBookDetailOnVisible(bookId = book.id)

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
                    modifier = modifier,
                    title = book.title,
                    authorName = authorName,
                    onClick = { onBookClick(book) },
                ) { coverModifier ->
                    DeadlineCoverOverlay(progress = deadlineProgress) {
                        EditionImage(
                            edition = currentEdition,
                            modifier = coverModifier,
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

            LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
            LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
                -> {
                CoverOnlyCell(
                    modifier = modifier,
                    onClick = { onBookClick(book) },
                ) { coverModifier ->
                    DeadlineCoverOverlay(progress = deadlineProgress) {
                        EditionImage(
                            edition = currentEdition,
                            modifier = coverModifier,
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

            LibraryGridLayout.LIST_COMPACT -> {
                val isRead = book.status == BookStatus.Read

                SwipeRowActions(
                    modifier = modifier.shakeOnError(
                        trigger = swipeFailed,
                        onShakeEnd = onSwipeShakeEnd,
                    ),
                    onMarkAsRead = onSwipeMarkAsRead,
                    onRemove = onSwipeRemove,
                    allowMarkAsRead = isRead.not(),
                ) {
                    CompactRow(
                        title = book.title,
                        authorName = authorName,
                        onClick = { onBookClick(book) },
                        deadlineProgress = deadlineProgress,
                    )
                }
            }

            LibraryGridLayout.LIST_LARGE -> {
                val isRead = book.status == BookStatus.Read

                SwipeRowActions(
                    modifier = modifier.shakeOnError(
                        trigger = swipeFailed,
                        onShakeEnd = onSwipeShakeEnd,
                    ),
                    onMarkAsRead = onSwipeMarkAsRead,
                    onRemove = onSwipeRemove,
                    allowMarkAsRead = isRead.not(),
                    backgroundShape = RoundedCornerShape(20.dp),
                ) {
                    LargeRow(
                        title = book.title,
                        authorName = currentEdition?.authorString.orEmpty(),
                        onClick = { onBookClick(book) },
                        seriesText = book.seriesText,
                        releaseYear = book.releaseYear,
                        usersCount = book.usersCount,
                        rating = book.rating,
                        deadlineProgress = deadlineProgress,
                        dateStyle = dateStyle,
                    ) { coverModifier ->
                        DeadlineCoverOverlay(progress = deadlineProgress) {
                            EditionImage(
                                edition = currentEdition,
                                modifier = coverModifier,
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
        PrefetchBookDetailOnVisible(bookId = edition.bookId)

        val title = edition.title.orEmpty()
        val authorName = edition.authors.map { it.name }.firstOrNull().orEmpty()

        when (layout) {
            LibraryGridLayout.GRID_TWO_COLUMNS,
            LibraryGridLayout.GRID_THREE_COLUMNS,
                -> {
                GridBookCell(
                    modifier = modifier,
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
                        ),
                    )
                }
            }

            LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
            LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
                -> {
                CoverOnlyCell(
                    modifier = modifier,
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
                        ),
                    )
                }
            }

            LibraryGridLayout.LIST_COMPACT -> {
                CompactRow(
                    modifier = modifier,
                    title = title,
                    authorName = authorName,
                    onClick = { onEditionClick(edition) },
                )
            }

            LibraryGridLayout.LIST_LARGE -> {
                LargeRow(
                    modifier = modifier,
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
        cover: @Composable (Modifier) -> Unit,
    ) {
        cover(
            modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 2f / 3f)
                .pressScaleClickable(onClick = onClick)
        )
    }

    @Composable
    private fun GridBookCell(
        title: String,
        authorName: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        cover: @Composable (Modifier) -> Unit,
    ) {
        Column(
            modifier = modifier.pressScaleClickable(onClick = onClick)
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
        deadlineProgress: DeadlineProgress? = null,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .pressScaleClickable(onClick = onClick),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                .pressScaleClickable(onClick = onClick),
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
