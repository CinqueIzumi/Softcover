package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.IndicatorBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.InlineErrorState
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.designsystem.presentation.component.OfflineScreenContent
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverSearchTopBar
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.feature.explore.data.mock.ExploreMockData
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnAddBookToLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnClearSearchAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnLoadMoreSearchResultsAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnQueryChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveBookFromLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRetrySearchAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnSearchActivatedAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnSearchDismissedAction
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreSearchPhase

// File-level so scroll position survives recomposition when the shared tab body movableContent moves
// between the compact/rail/sidebar chrome layouts on a window resize.
private val editorialScrollState = ScrollState(initial = 0)
private val focusScrollState = ScrollState(initial = 0)
private val trendingListState = LazyListState()
private val continueSeriesListState = LazyListState()
private val becauseYouReadListState = LazyListState()

private val TRENDING_CARD_WIDTH = 118.dp
private val BECAUSE_YOU_READ_CARD_WIDTH = 118.dp
private val UP_NEXT_CARD_WIDTH = 126.dp

/**
 * Mobile Explore (explore-3a): a [SoftcoverSearchTopBar] search chrome over one of four bodies,
 * branched on [ExploreScreenUiState.searchPhase] — the editorial feed, the search-focus overlay
 * (recent + "try a mood"), the loading state, or the results list. The cards, the dismiss sheet, the
 * mood grid, and the recent-searches block are the shared shelf pieces (`ExploreShelf.kt`); only this
 * Scaffold-and-phase framing is mobile-specific.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal actual fun ExploreScreenLayout(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
    onScanClick: () -> Unit,
    isOnline: Boolean,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SoftcoverSearchTopBar(
                searchText = state.searchText,
                onSearchValueChange = { runAction(OnQueryChangeAction(newQuery = it)) },
                onScanClick = onScanClick,
                isLoading = state.isLoading,
                active = state.searchPhase != ExploreSearchPhase.FEED,
                focused = state.searchFocused,
                onSearchActivated = { runAction(OnSearchActivatedAction) },
                onSearchDismissed = { runAction(OnSearchDismissedAction) },
                onClearSearch = { runAction(OnClearSearchAction) },
            )
        },
    ) { padding ->
        if (isOnline.not()) {
            OfflineScreenContent(
                modifier = Modifier
                    .padding(padding)
                    .padding(bottom = rememberBottomBarPadding()),
            )
            return@Scaffold
        }

        when (state.searchPhase) {
            ExploreSearchPhase.FEED -> EditorialContent(
                state = state,
                runAction = runAction,
                onBookClick = onBookClick,
                contentPadding = padding,
            )

            ExploreSearchPhase.FOCUS -> SearchFocusScreen(
                state = state,
                runAction = runAction,
                contentPadding = padding,
            )

            ExploreSearchPhase.LOADING, ExploreSearchPhase.RESULTS -> ActiveSearchContent(
                state = state,
                runAction = runAction,
                onBookClick = onBookClick,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun SearchFocusScreen(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    contentPadding: PaddingValues,
) {
    Box(
        // Feedback item 11: tapping outside the focused field dismisses the focus surface. Only the
        // state is dismissed here - the top bar owns the platform focus and the keyboard, and lets
        // go of both off the back of this action (see `SoftcoverSearchTopBar`). Clearing focus from
        // this side as well is what used to leave the two out of step.
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { runAction(OnSearchDismissedAction) }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(focusScrollState)
                .padding(top = 18.dp),
        ) {
            SearchFocusContent(
                queries = state.previousSearchQueries,
                moods = state.moodTags,
                runAction = runAction,
            )

            Spacer(modifier = Modifier.height(rememberBottomBarPadding()))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EditorialContent(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
    contentPadding: PaddingValues,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { runAction(OnRefreshAction) },
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize(),
        state = pullToRefreshState,
        indicator = {
            IndicatorBox(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState,
                isRefreshing = state.isRefreshing,
            ) {
                ContainedLoadingIndicator(modifier = Modifier.align(Alignment.TopCenter))
            }
        },
    ) {
        Column(
            // The opening 8dp is padding, not a leading Spacer: a Spacer is a child like any other,
            // so `spacedBy(36.dp)` used to add its full gap *between* it and the featured card,
            // costing 44dp under the search chrome instead of the 8 it looked like it asked for.
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(editorialScrollState)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            FeaturedSection(
                book = state.featuredUpcomingRelease,
                isLoading = state.loadingFeaturedUpcomingRelease && state.featuredUpcomingRelease == null,
                onBookClick = onBookClick,
                runAction = runAction,
            )

            ContinueSeriesSection(
                books = state.continueSeriesBooks,
                isLoading = state.loadingContinueSeriesBooks && state.continueSeriesBooks.isEmpty(),
                onBookClick = onBookClick,
                runAction = runAction,
            )

            BecauseYouReadSection(
                genre = state.becauseYouReadGenre,
                genreOptions = state.becauseYouReadGenreOptions,
                books = state.becauseYouReadBooks,
                isLoading = state.loadingBecauseYouReadBooks && state.becauseYouReadBooks.isEmpty(),
                onBookClick = onBookClick,
                runAction = runAction,
            )

            TrendingSection(
                books = state.trendingBooks,
                isLoading = state.loadingTrendingBooks && state.trendingBooks.isEmpty(),
                onBookClick = onBookClick,
            )

            MoodGrid(
                moods = state.moodTags,
                isLoading = state.loadingMoodTags && state.moodTags.isEmpty(),
                runAction = runAction,
            )

            RecentSearchesSection(
                queries = state.previousSearchQueries,
                runAction = runAction,
            )

            Spacer(modifier = Modifier.height(rememberBottomBarPadding()))
        }
    }
}

@Composable
private fun FeaturedSection(
    book: Book?,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    // Terminal empty (no upcoming release to feature) collapses away with no reserved space;
    // every other state - loading or loaded - keeps the card's own footprint so the crossfade
    // below never resizes the feed around it.
    if (isLoading.not() && book == null) return

    // No EditorialSectionHeader above this one, unlike every rail below it: the card names itself
    // with an inline eyebrow on its own top row (see FeaturedCard), which keeps the feed's opening
    // screen for the book rather than for a header introducing it.
    SkeletonCrossfade(
        isLoading = isLoading,
        modifier = Modifier.padding(horizontal = 16.dp),
        label = "FeaturedSection",
    ) { loading ->
        if (loading) {
            FeaturedCardSkeleton()
        } else if (book != null) {
            FeaturedCard(
                book = book,
                onClick = {
                    onBookClick(
                        book,
                        SURFACE_FEATURED,
                    )
                },
                onWantToReadClick = { runAction(OnAddBookToLibraryClickAction(book = book)) },
                onRemoveFromLibraryClick = { runAction(OnRemoveBookFromLibraryClickAction(book = book)) },
            )
        }
    }
}

@Composable
private fun TrendingSection(
    books: List<Book>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
) {
    if (isLoading.not() && books.isEmpty()) return

    // Hoisted above the crossfade so the coordinator's first-composition timestamp is stamped as
    // soon as this section mounts - typically while still loading - not at the moment content
    // arrives. By the time a real network fetch resolves, the stagger window has usually already
    // elapsed, so the item entrance defers entirely to the crossfade below instead of double-
    // animating alongside it; the stagger only plays when content is already on hand at the
    // section's very first paint (design-system.md §2.5 "Staggered entry on first composition").
    val entry = rememberStaggeredEntryCoordinator(key = "explore:trending")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        EditorialSectionHeader(
            eyebrow = "Trending this week",
            headline = "What everyone's reading",
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        SkeletonCrossfade(
            isLoading = isLoading,
            label = "TrendingRail",
        ) { loading ->
            if (loading) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(TRENDING_SKELETON_COUNT) {
                        TrendingCardSkeleton(modifier = Modifier.width(TRENDING_CARD_WIDTH))
                    }
                }
            } else {
                LazyRow(
                    state = trendingListState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(books, key = { _, book -> book.id }) { index, book ->
                        TrendingCard(
                            modifier = Modifier
                                .width(TRENDING_CARD_WIDTH)
                                .staggeredEntry(coordinator = entry, index = index),
                            book = book,
                            onClick = {
                                onBookClick(
                                    book,
                                    SURFACE_TRENDING,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BecauseYouReadSection(
    genre: String?,
    genreOptions: List<String>,
    books: List<Book>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    // Terminal empty: the fetch is done and this reader has no genre to recommend from (or,
    // defensively, no books despite a resolved genre) - nothing to show, so the section collapses
    // with no reserved space. Any other state - still loading, regardless of whether genre/books
    // happen to have arrived yet - keeps the header+rail's full footprint reserved below, which is
    // what fixes the section popping into the feed at full height once the genre resolves: it used
    // to gate on `if (genre == null) return` unconditionally, hiding the whole section - header
    // included - for the entire loading window, not just the genuinely-empty terminal state.
    if (isLoading.not() && (genre == null || books.isEmpty())) return

    // Hoisted above the crossfade - see TrendingSection's comment for why.
    val entry = rememberStaggeredEntryCoordinator(key = "explore:because_you_read")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SkeletonCrossfade(
            isLoading = isLoading,
            modifier = Modifier.padding(horizontal = 24.dp),
            label = "BecauseYouReadHeader",
        ) { loading ->
            if (loading) {
                EditorialSectionHeaderSkeleton()
            } else if (genre != null) {
                EditorialSectionHeader(
                    eyebrow = "Because you read",
                    headline = genre,
                )
            }
        }

        // Crossfaded too, not just gated on `genre != null` directly: genre and `isLoading` flip
        // together (see the guard above), so an ungated appearance would hard-pop this control in
        // alongside the header/rail's smooth 150ms fade — a small but visible inconsistency next
        // to the very transition this file's crossfade work exists to smooth out.
        SkeletonCrossfade(
            isLoading = isLoading,
            modifier = Modifier.padding(horizontal = 24.dp),
            label = "BecauseYouReadGenreControl",
        ) { loading ->
            if (loading.not() && genre != null) {
                BecauseYouReadGenreControl(
                    genre = genre,
                    options = genreOptions,
                    runAction = runAction,
                )
            }
        }

        SkeletonCrossfade(
            isLoading = isLoading,
            label = "BecauseYouReadRail",
        ) { loading ->
            if (loading) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(BECAUSE_YOU_READ_SKELETON_COUNT) {
                        BecauseYouReadCardSkeleton(modifier = Modifier.width(BECAUSE_YOU_READ_CARD_WIDTH))
                    }
                }
            } else {
                LazyRow(
                    state = becauseYouReadListState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(books, key = { _, book -> book.id }) { index, book ->
                        BecauseYouReadCard(
                            modifier = Modifier
                                .width(BECAUSE_YOU_READ_CARD_WIDTH)
                                .staggeredEntry(coordinator = entry, index = index),
                            book = book,
                            onClick = {
                                onBookClick(
                                    book,
                                    SURFACE_BECAUSE_YOU_READ,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueSeriesSection(
    books: List<Book>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    if (isLoading.not() && books.isEmpty()) return

    var sheetBook by remember { mutableStateOf<Book?>(null) }

    // Hoisted above the crossfade - see TrendingSection's comment for why.
    val entry = rememberStaggeredEntryCoordinator(key = "explore:continue_series")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        EditorialSectionHeader(
            eyebrow = "Pick up where you left off",
            headline = "Up next in your series",
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        SkeletonCrossfade(
            isLoading = isLoading,
            label = "ContinueSeriesRail",
        ) { loading ->
            if (loading) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(CONTINUE_SERIES_SKELETON_COUNT) {
                        SeriesCardSkeleton(modifier = Modifier.width(UP_NEXT_CARD_WIDTH))
                    }
                }
            } else {
                LazyRow(
                    state = continueSeriesListState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(books, key = { _, book -> book.id }) { index, book ->
                        val cardModifier = Modifier
                            .width(UP_NEXT_CARD_WIDTH)
                            .staggeredEntry(coordinator = entry, index = index)

                        if (book.isUnreleased) {
                            UnreleasedSeriesCard(
                                modifier = cardModifier,
                                book = book,
                                onClick = {
                                    onBookClick(
                                        book,
                                        SURFACE_UP_NEXT,
                                    )
                                },
                                onMenuClick = { sheetBook = book },
                            )
                        } else {
                            SeriesCard(
                                modifier = cardModifier,
                                book = book,
                                onClick = {
                                    onBookClick(
                                        book,
                                        SURFACE_UP_NEXT,
                                    )
                                },
                                onMenuClick = { sheetBook = book },
                            )
                        }
                    }
                }
            }
        }
    }

    sheetBook?.let { book ->
        ContinueSeriesMenuSheet(
            book = book,
            runAction = runAction,
            onDismiss = { sheetBook = null },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActiveSearchContent(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize(),
    ) {
        if (state.searchError != null) {
            InlineErrorState(
                message = state.searchError,
                onRetry = { runAction(OnRetrySearchAction) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                textStyle = MaterialTheme.editorialTypography.bodySmall,
            )

            return
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            SortChip(
                sortMode = state.sortMode,
                runAction = runAction,
            )
        }

        val subtitle = when {
            state.searchText.isNotEmpty() -> "for \"${state.searchText}\""
            state.activeMoodFilter != null -> "for \"${state.activeMoodFilter.label}\""
            else -> null
        }

        SearchResultsHeader(
            resultCount = state.queriedBooks.size,
            subtitle = subtitle,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        val resultsListState = rememberLazyListState()

        // Feedback item 7: append the next page once the visible window nears the fetched end.
        // There's no server-side total (Typesense returns no hit count), so `queriedBooksHasMore`
        // is the only stop condition — the action itself is the re-entrancy guard (a no-op while a
        // fetch is already in flight), so this can dispatch freely on every qualifying scroll frame.
        LaunchedEffect(resultsListState, state.queriedBooks.size, state.queriedBooksHasMore) {
            val itemCount = state.queriedBooks.size
            if (state.queriedBooksHasMore.not() || itemCount == 0) return@LaunchedEffect

            snapshotFlow { resultsListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastVisibleIndex ->
                    val nearEnd = lastVisibleIndex != null &&
                        lastVisibleIndex >= itemCount - SEARCH_RESULTS_LOAD_MORE_THRESHOLD

                    if (nearEnd) {
                        runAction(OnLoadMoreSearchResultsAction)
                    }
                }
        }

        LazyColumn(
            state = resultsListState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = rememberBottomBarPadding()),
        ) {
            items(state.queriedBooks, key = { it.id }) { book ->
                SearchResultRow(
                    book = book,
                    onBookClick = onBookClick,
                    runAction = runAction,
                )
            }

            if (state.loadingMoreQueriedBooks) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularWavyProgressIndicator()
                    }
                }
            }
        }
    }
}

private val previewMockState = ExploreScreenUiState(
    previousSearchQueries = listOf(
        "Bubblegum",
        "Earthlings",
        "Convenience Store",
        "Babel",
        "Piranesi",
    ),
    trendingBooks = ExploreMockData.trending,
    loadingTrendingBooks = false,
    continueSeriesBooks = ExploreMockData.continueSeries,
    loadingContinueSeriesBooks = false,
    loadingFeaturedUpcomingRelease = false,
    loadingBecauseYouReadBooks = false,
    loadingMoodTags = false,
)

@StandardPreview
@Composable
private fun ExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreenLayout(
            runAction = {},
            onBookClick = { _, _ -> },
            onScanClick = {},
            isOnline = true,
            state = previewMockState,
        )
    }
}

@StandardPreview
@Composable
private fun EmptyFirstLaunchExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreenLayout(
            runAction = {},
            onBookClick = { _, _ -> },
            onScanClick = {},
            isOnline = true,
            state = ExploreScreenUiState(
                trendingBooks = ExploreMockData.trending,
                loadingTrendingBooks = false,
                continueSeriesBooks = emptyList(),
                loadingContinueSeriesBooks = false,
                previousSearchQueries = emptyList(),
                loadingFeaturedUpcomingRelease = false,
                loadingBecauseYouReadBooks = false,
                loadingMoodTags = false,
            ),
        )
    }
}

@StandardPreview
@Composable
private fun LoadingTrendingExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreenLayout(
            runAction = {},
            onBookClick = { _, _ -> },
            onScanClick = {},
            isOnline = true,
            state = ExploreScreenUiState(
                trendingBooks = emptyList(),
                loadingTrendingBooks = true,
                continueSeriesBooks = ExploreMockData.continueSeries,
                loadingContinueSeriesBooks = false,
                previousSearchQueries = listOf("Bubblegum", "Earthlings"),
                loadingFeaturedUpcomingRelease = false,
                loadingBecauseYouReadBooks = false,
                loadingMoodTags = false,
            ),
        )
    }
}

@StandardPreview
@Composable
private fun LoadingContinueSeriesExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreenLayout(
            runAction = {},
            onBookClick = { _, _ -> },
            onScanClick = {},
            isOnline = true,
            state = ExploreScreenUiState(
                trendingBooks = ExploreMockData.trending,
                loadingTrendingBooks = false,
                continueSeriesBooks = emptyList(),
                loadingContinueSeriesBooks = true,
                previousSearchQueries = emptyList(),
                loadingFeaturedUpcomingRelease = false,
                loadingBecauseYouReadBooks = false,
                loadingMoodTags = false,
            ),
        )
    }
}

@StandardPreview
@Composable
private fun SearchFocusExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreenLayout(
            runAction = {},
            onBookClick = { _, _ -> },
            onScanClick = {},
            isOnline = true,
            state = previewMockState.copy(
                searchFocused = true,
                moodTags = listOf(
                    MoodTag(
                        id = 1,
                        label = "Cosy & comforting",
                        slug = "cosy",
                        bookCount = 820,
                    ),
                    MoodTag(
                        id = 2,
                        label = "Dread & unease",
                        slug = "dread",
                        bookCount = 1540,
                    ),
                ),
            ),
        )
    }
}

@StandardPreview
@Composable
private fun OfflineExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreenLayout(
            runAction = {},
            onBookClick = { _, _ -> },
            onScanClick = {},
            isOnline = false,
            state = previewMockState,
        )
    }
}

@StandardPreview
@Composable
private fun ActiveExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreenLayout(
            runAction = {},
            onBookClick = { _, _ -> },
            onScanClick = {},
            isOnline = true,
            state = ExploreScreenUiState(
                searchText = "Last to leave",
                queriedBooks = listOf(
                    PreviewData.baseBook.copy(
                        title = "Last to Leave the Room",
                        defaultEdition = PreviewData.baseEdition.copy(releaseYear = 2023),
                        rating = 3.7,
                        bookSeries = BookSeries(
                            id = 1,
                            name = "Starling",
                            amountOfBooks = 20,
                        ),
                    ),
                    PreviewData.baseBook.copy(
                        id = 2,
                        title = "The Last to Leave",
                        defaultEdition = PreviewData.baseEdition.copy(releaseYear = 2021),
                        rating = 4.2,
                        userBook = PreviewData.baseBook.userBook,
                        bookSeries = BookSeries(
                            id = 1,
                            name = "Starling",
                            amountOfBooks = 20,
                        ),
                        positionsInSeries = listOf(2.0),
                    ),
                    PreviewData.baseBook.copy(
                        id = 3,
                        title = "Last One to Leave",
                        defaultEdition = PreviewData.baseEdition.copy(releaseYear = 2022),
                        rating = 4.0,
                    ),
                    PreviewData.baseBook.copy(
                        id = 4,
                        title = "Will the Last Person To Leave the Planet Please Shut Off the Sun",
                        defaultEdition = PreviewData.baseEdition.copy(releaseYear = 2021),
                        rating = 0.0,
                    ),
                ),
            ),
        )
    }
}

@StandardPreview
@Composable
private fun LoadingActiveExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreenLayout(
            runAction = {},
            onBookClick = { _, _ -> },
            onScanClick = {},
            isOnline = true,
            state = ExploreScreenUiState(
                searchText = "Piranesi",
                queriedBooks = emptyList(),
                isLoading = true,
            ),
        )
    }
}

@StandardPreview
@Composable
private fun NoResultsActiveExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreenLayout(
            runAction = {},
            onBookClick = { _, _ -> },
            onScanClick = {},
            isOnline = true,
            state = ExploreScreenUiState(
                searchText = "qwertyuiop",
                queriedBooks = emptyList(),
                isLoading = false,
            ),
        )
    }
}
