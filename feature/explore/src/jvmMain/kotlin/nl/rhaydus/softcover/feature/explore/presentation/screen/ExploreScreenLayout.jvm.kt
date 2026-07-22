package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.editorial.component.EditorialSearchField
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.designsystem.presentation.component.OfflineScreenContent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnAddBookToLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnLoadMoreSearchResultsAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnQueryChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveBookFromLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

private val discoveryScrollState = ScrollState(initial = 0)
private val searchResultsGridState = LazyGridState()

private val DESKTOP_TRENDING_CARD_WIDTH = 168.dp
private val DESKTOP_UP_NEXT_CARD_WIDTH = 150.dp
private const val DESKTOP_DISCOVERY_SKELETON_COUNT = 8

/**
 * Desktop Explore (explore-3a): a full-width discovery surface (no two-pane — a tapped book pushes
 * detail full-screen, see `wideGridTabsUseDetailPane`). A static editorial header carries an explicit
 * Refresh control and the barcode-scan affordance; a persistent [EditorialSearchField] sits beneath it
 * (desktop has no search-focus takeover — the field is always in place, per the foundation's persistent-
 * field pattern for a static editorial header). With the field empty and no mood filter active, the body
 * is the discovery wall: the featured release, then trending / because-you-read / up-next rendered as
 * wrapping multi-column grids ([FlowRow] of the shared cards), the mood grid, and recent-search history —
 * all scrolled by a [DesktopVerticalScrollbar]. With a query or mood filter active the body becomes a
 * multi-column results grid. No pager and no pull-to-refresh. The cards, dismiss sheet, mood grid, and
 * recent-searches block are the shared shelf pieces.
 */
@Composable
internal actual fun ExploreScreenLayout(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
    onScanClick: () -> Unit,
    isOnline: Boolean,
) {
    if (isOnline.not()) {
        OfflineScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = rememberBottomBarPadding()),
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DesktopExploreHeader(
            isRefreshing = state.isRefreshing,
            onRefreshClick = { runAction(OnRefreshAction) },
            onScanClick = onScanClick,
        )

        Spacer(modifier = Modifier.height(8.dp))

        EditorialSearchField(
            query = state.searchText,
            onQueryChange = { query ->
                runAction(OnQueryChangeAction(newQuery = query))
            },
            onClearClick = {
                runAction(OnQueryChangeAction(newQuery = ""))
            },
            searchIcon = drawableIconResource(
                icon = SoftcoverIcon.Search,
                contentDescription = "Search",
            ),
            clearIcon = drawableIconResource(
                icon = SoftcoverIcon.Close,
                contentDescription = "Clear search",
            ),
            placeholder = "Search books, authors…",
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                state.searchText.isEmpty() && state.activeMoodFilter == null -> DesktopDiscovery(
                    state = state,
                    runAction = runAction,
                    onBookClick = onBookClick,
                )

                else -> DesktopSearchResults(
                    state = state,
                    runAction = runAction,
                    onBookClick = onBookClick,
                )
            }
        }
    }
}

@Composable
private fun DesktopExploreHeader(
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit,
    onScanClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, top = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isRefreshing) "Finding fresh picks…" else "Discover",
                style = MaterialTheme.editorialTypography.eyebrow,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Explore",
                style = MaterialTheme.editorialTypography.pageTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 16.dp),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Trending books, the next in your series, and anything you search for.",
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

            DesktopTooltip(text = "Scan a barcode") {
                IconButton(
                    onClick = onScanClick,
                    modifier = Modifier.pointerHandCursor(),
                ) {
                    val scanIcon = drawableIconResource(
                        icon = SoftcoverIcon.BarcodeScanner,
                        contentDescription = "Scan a book's barcode",
                    )

                    Icon(
                        painter = scanIcon.getIconPainter(),
                        contentDescription = scanIcon.contentDescription,
                    )
                }
            }
        }
    }
}

@Composable
private fun DesktopDiscovery(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(discoveryScrollState)
                .padding(bottom = 24.dp + rememberBottomBarPadding()),
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            DesktopFeaturedSection(
                book = state.featuredUpcomingRelease,
                isLoading = state.loadingFeaturedUpcomingRelease && state.featuredUpcomingRelease == null,
                onBookClick = onBookClick,
                runAction = runAction,
            )

            DesktopUpNextSection(
                books = state.continueSeriesBooks,
                isLoading = state.loadingContinueSeriesBooks && state.continueSeriesBooks.isEmpty(),
                onBookClick = onBookClick,
                runAction = runAction,
            )

            DesktopBecauseYouReadSection(
                genre = state.becauseYouReadGenre,
                genreOptions = state.becauseYouReadGenreOptions,
                books = state.becauseYouReadBooks,
                isLoading = state.loadingBecauseYouReadBooks && state.becauseYouReadBooks.isEmpty(),
                onBookClick = onBookClick,
                runAction = runAction,
            )

            DesktopTrendingSection(
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
        }

        DesktopVerticalScrollbar(
            scrollState = discoveryScrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = 4.dp),
        )
    }
}

@Composable
private fun DesktopFeaturedSection(
    book: Book?,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    // Terminal empty (no upcoming release to feature) collapses away with no reserved space;
    // every other state - loading or loaded - keeps the card's own footprint so the crossfade
    // below never resizes the feed around it.
    if (isLoading.not() && book == null) return

    // No SectionHeaderBar above this one - the card names itself with an inline eyebrow; see the
    // mobile FeaturedSection for why.
    SkeletonCrossfade(
        isLoading = isLoading,
        modifier = Modifier.padding(horizontal = 24.dp),
        label = "DesktopFeaturedSection",
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DesktopTrendingSection(
    books: List<Book>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
) {
    if (isLoading.not() && books.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeaderBar(
            eyebrow = "Trending this week",
            headline = "What everyone's reading",
        )

        SkeletonCrossfade(
            isLoading = isLoading,
            label = "DesktopTrendingRail",
        ) { loading ->
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (loading) {
                    repeat(DESKTOP_DISCOVERY_SKELETON_COUNT) {
                        TrendingCardSkeleton(modifier = Modifier.width(DESKTOP_TRENDING_CARD_WIDTH))
                    }
                } else {
                    books.forEach { book ->
                        TrendingCard(
                            modifier = Modifier.width(DESKTOP_TRENDING_CARD_WIDTH),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DesktopBecauseYouReadSection(
    genre: String?,
    genreOptions: List<String>,
    books: List<Book>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    // See the mobile BecauseYouReadSection's comment: the section only collapses once the fetch
    // is done and there's genuinely nothing to show (no genre, or no books despite a genre) -
    // any other state, including "still loading with genre unresolved", keeps the header+rail's
    // full footprint reserved so the section never pops into the feed at full height once the
    // genre resolves.
    if (isLoading.not() && (genre == null || books.isEmpty())) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SkeletonCrossfade(
            isLoading = isLoading,
            modifier = Modifier.padding(horizontal = 24.dp),
            label = "DesktopBecauseYouReadHeader",
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

        // Crossfaded too, not just gated on `genre != null` directly - see the mobile
        // BecauseYouReadSection's comment: genre and `isLoading` flip together, so an ungated
        // appearance would hard-pop this control in alongside the header/rail's smooth fade.
        SkeletonCrossfade(
            isLoading = isLoading,
            modifier = Modifier.padding(horizontal = 24.dp),
            label = "DesktopBecauseYouReadGenreControl",
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
            label = "DesktopBecauseYouReadRail",
        ) { loading ->
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (loading) {
                    repeat(DESKTOP_DISCOVERY_SKELETON_COUNT) {
                        BecauseYouReadCardSkeleton(modifier = Modifier.width(DESKTOP_TRENDING_CARD_WIDTH))
                    }
                } else {
                    books.forEach { book ->
                        BecauseYouReadCard(
                            modifier = Modifier.width(DESKTOP_TRENDING_CARD_WIDTH),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DesktopUpNextSection(
    books: List<Book>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    if (isLoading.not() && books.isEmpty()) return

    var sheetBook by remember { mutableStateOf<Book?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeaderBar(
            eyebrow = "Pick up where you left off",
            headline = "Up next in your series",
        )

        SkeletonCrossfade(
            isLoading = isLoading,
            label = "DesktopUpNextRail",
        ) { loading ->
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (loading) {
                    repeat(DESKTOP_DISCOVERY_SKELETON_COUNT) {
                        SeriesCardSkeleton(modifier = Modifier.width(DESKTOP_UP_NEXT_CARD_WIDTH))
                    }
                } else {
                    books.forEach { book ->
                        val cardModifier = Modifier.width(DESKTOP_UP_NEXT_CARD_WIDTH)

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

/** The 32×4 accent-bar section opener (design-system.md §2.3), padded to the desktop grid gutter. */
@Composable
private fun SectionHeaderBar(
    eyebrow: String,
    headline: String,
) {
    EditorialSectionHeader(
        eyebrow = eyebrow,
        headline = headline,
        modifier = Modifier.padding(horizontal = 24.dp),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DesktopSearchResults(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
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
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback item 7: same append-on-scroll-near-end trigger as mobile's LazyColumn, driven off
        // this grid's own `searchResultsGridState` instead of a per-composition list state.
        LaunchedEffect(state.queriedBooks.size, state.queriedBooksHasMore) {
            val itemCount = state.queriedBooks.size
            if (state.queriedBooksHasMore.not() || itemCount == 0) return@LaunchedEffect

            snapshotFlow { searchResultsGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastVisibleIndex ->
                    val nearEnd = lastVisibleIndex != null &&
                        lastVisibleIndex >= itemCount - SEARCH_RESULTS_LOAD_MORE_THRESHOLD

                    if (nearEnd) {
                        runAction(OnLoadMoreSearchResultsAction)
                    }
                }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            LazyVerticalGrid(
                state = searchResultsGridState,
                columns = GridCells.Adaptive(minSize = 380.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = rememberBottomBarPadding()),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(state.queriedBooks, key = { it.id }) { book ->
                    SearchResultRow(
                        book = book,
                        onBookClick = onBookClick,
                        runAction = runAction,
                    )
                }

                if (state.loadingMoreQueriedBooks) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
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

            DesktopVerticalScrollbar(
                gridState = searchResultsGridState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
            )
        }
    }
}
