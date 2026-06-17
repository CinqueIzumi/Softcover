package nl.rhaydus.softcover.feature.explore.presentation.screen

import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.IndicatorBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.component.OfflineScreenContent
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverSearchTopBar
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBarAction
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.util.rememberBottomBarPadding
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.feature.explore.data.mock.ExploreMockData
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnQueryChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

// File-level so scroll position survives recomposition when the shared tab body movableContent moves
// between the compact/rail/sidebar chrome layouts on a window resize.
private val editorialScrollState = ScrollState(initial = 0)
private val trendingListState = LazyListState()
private val continueSeriesListState = LazyListState()

/**
 * Mobile Explore: the search-first Scaffold — a [SoftcoverSearchTopBar] over either the editorial
 * discovery feed (trending + up-next carousels + recent searches, with pull-to-refresh) or the active
 * search results list. The cards, the dismiss sheet, and the recent-searches block are the shared
 * shelf pieces ([TrendingCard], [SeriesCard], [ContinueSeriesMenuSheet], [RecentSearchesSection],
 * [SearchResultRow]); only this carousel-and-Scaffold framing is mobile-specific.
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
                onNavigateBack = null,
                searchText = state.searchText,
                onSearchValueChange = {
                    runAction(OnQueryChangeAction(newQuery = it))
                },
                isLoading = state.isLoading,
                trailingFieldAction = SoftcoverTopBarAction(
                    iconResource = drawableIconResource(
                        icon = SoftcoverIcon.BarcodeScanner,
                        contentDescription = "Scan a book's barcode",
                    ),
                    onClick = onScanClick,
                ),
            )
        },
    ) { padding ->
        if (isOnline.not()) {
            OfflineScreenContent(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        when {
            state.searchText.isEmpty() -> EditorialContent(
                state = state,
                runAction = runAction,
                onBookClick = onBookClick,
                contentPadding = padding,
            )

            else -> ActiveSearchContent(
                state = state,
                runAction = runAction,
                onBookClick = onBookClick,
                contentPadding = padding,
            )
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(editorialScrollState),
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TrendingSection(
                books = state.trendingBooks,
                isLoading = state.loadingTrendingBooks && state.trendingBooks.isEmpty(),
                onBookClick = onBookClick,
            )

            ContinueSeriesSection(
                books = state.continueSeriesBooks,
                isLoading = state.loadingContinueSeriesBooks && state.continueSeriesBooks.isEmpty(),
                onBookClick = onBookClick,
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
private fun TrendingSection(
    books: List<Book>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
) {
    if (isLoading.not() && books.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        EditorialSectionHeader(
            eyebrow = "This week",
            headline = "Trending",
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        SkeletonCrossfade(
            isLoading = isLoading,
            label = "TrendingRow",
        ) { loading ->
            if (loading) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(TRENDING_SKELETON_COUNT) {
                        TrendingCardSkeleton(modifier = Modifier.width(150.dp))
                    }
                }
            } else {
                val entry = rememberStaggeredEntryCoordinator(key = "explore:trending")

                LazyRow(
                    state = trendingListState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(books, key = { _, book -> book.id }) { index, book ->
                        TrendingCard(
                            modifier = Modifier
                                .width(150.dp)
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
private fun ContinueSeriesSection(
    books: List<Book>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    if (isLoading.not() && books.isEmpty()) return

    var sheetBook by remember { mutableStateOf<Book?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        EditorialSectionHeader(
            eyebrow = "Pick up where you left off",
            headline = "Up next in your series",
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        SkeletonCrossfade(
            isLoading = isLoading,
            label = "ContinueSeriesRow",
        ) { loading ->
            if (loading) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(CONTINUE_SERIES_SKELETON_COUNT) {
                        SeriesCardSkeleton(modifier = Modifier.width(120.dp))
                    }
                }
            } else {
                val entry = rememberStaggeredEntryCoordinator(key = "explore:continue_series")

                LazyRow(
                    state = continueSeriesListState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(books, key = { _, book -> book.id }) { index, book ->
                        SeriesCard(
                            modifier = Modifier
                                .width(120.dp)
                                .staggeredEntry(coordinator = entry, index = index),
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

    sheetBook?.let { book ->
        ContinueSeriesMenuSheet(
            book = book,
            runAction = runAction,
            onDismiss = { sheetBook = null },
        )
    }
}

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
            .padding(horizontal = 24.dp)
            .fillMaxSize(),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        val text = when {
            state.isLoading -> "Loading..."
            state.queriedBooks.isEmpty() -> "No results found"
            else -> "Showing ${state.queriedBooks.size} results"
        }

        Text(
            text = text,
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(state.queriedBooks, key = { it.id }) { book ->
                SearchResultRow(
                    book = book,
                    onBookClick = onBookClick,
                    runAction = runAction,
                )
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
