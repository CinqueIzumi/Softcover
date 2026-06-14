package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.component.DesktopVerticalScrollbar
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditorialSearchField
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.component.OfflineScreenContent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnQueryChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

private val discoveryScrollState = ScrollState(initial = 0)
private val searchResultsGridState = LazyGridState()

private val DESKTOP_TRENDING_CARD_WIDTH = 168.dp
private val DESKTOP_UP_NEXT_CARD_WIDTH = 150.dp
private const val DESKTOP_DISCOVERY_SKELETON_COUNT = 8

/**
 * Desktop Explore: a full-width discovery surface (no two-pane — a tapped book pushes detail
 * full-screen, see `wideGridTabsUseDetailPane`). A static editorial header (no search top bar) carries
 * an explicit Refresh control and the barcode-scan affordance; a persistent [EditorialSearchField]
 * sits beneath it. With the field empty the body is the discovery wall — trending and up-next rendered
 * as wrapping multi-column grids ([FlowRow] of shared cards) over the recent-search history, scrolled
 * by a [DesktopVerticalScrollbar]. With a query present the body becomes a multi-column results grid.
 * No pager and no pull-to-refresh. The cards, dismiss sheet, and recent-searches block are the shared
 * shelf pieces.
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
        OfflineScreenContent(modifier = Modifier.fillMaxSize())
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
            placeholder = "Search books, authors…",
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                state.searchText.isEmpty() -> DesktopDiscovery(
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

            IconButton(
                onClick = onScanClick,
                modifier = Modifier.pointerHandCursor(),
            ) {
                val scanIcon = SoftcoverIconResource.Drawable(
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
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            DesktopTrendingSection(
                books = state.trendingBooks,
                isLoading = state.loadingTrendingBooks && state.trendingBooks.isEmpty(),
                onBookClick = onBookClick,
            )

            DesktopUpNextSection(
                books = state.continueSeriesBooks,
                isLoading = state.loadingContinueSeriesBooks && state.continueSeriesBooks.isEmpty(),
                onBookClick = onBookClick,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DesktopTrendingSection(
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
            label = "DesktopTrendingGrid",
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
private fun DesktopUpNextSection(
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
            label = "DesktopUpNextGrid",
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
                        SeriesCard(
                            modifier = Modifier.width(DESKTOP_UP_NEXT_CARD_WIDTH),
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

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            LazyVerticalGrid(
                state = searchResultsGridState,
                columns = GridCells.Adaptive(minSize = 380.dp),
                modifier = Modifier.fillMaxSize(),
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
