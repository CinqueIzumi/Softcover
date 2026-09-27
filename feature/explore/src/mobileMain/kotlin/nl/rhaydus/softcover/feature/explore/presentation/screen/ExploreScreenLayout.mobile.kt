package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.component.state.offlineEmptyStateUiModel
import nl.rhaydus.softcover.core.component.topbar.SearchTopBar
import nl.rhaydus.softcover.core.component.topbar.SearchTopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.explore.data.mock.ExploreMockData
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnClearSearchAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnQueryChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnSearchActivatedAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnSearchDismissedAction
import nl.rhaydus.softcover.feature.explore.presentation.collector.MoodChipsSnapshot
import nl.rhaydus.softcover.feature.explore.presentation.collector.RecentSearchChipsSnapshot
import nl.rhaydus.softcover.feature.explore.presentation.screen.section.ActiveSearchContent
import nl.rhaydus.softcover.feature.explore.presentation.screen.section.EditorialContent
import nl.rhaydus.softcover.feature.explore.presentation.screen.section.SearchFocusScreen
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreSearchPhase

/**
 * Mobile Explore (explore-3a): a [SearchTopBar] search chrome over one of four bodies,
 * branched on [ExploreScreenUiState.searchPhase] — the editorial feed, the search-focus overlay
 * (recent + "try a mood"), the loading state, or the results list. The cards, the dismiss sheet, the
 * mood grid, and the recent-searches block are the shared shelf pieces; only this
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
    // Back leaves search before it leaves the screen, one rung per press. With a query or a mood
    // browse on screen the press clears it and hands the feed straight back - OnClearSearchAction
    // drops the chrome's focus along with the results, so there is no third rung to climb - and
    // with only the focus surface open the press closes that. At ExploreSearchPhase.FEED neither
    // handler is enabled, so back belongs to the shell again.
    //
    // Both go through an action rather than touching the field: the search chrome follows state,
    // and clearing platform focus from this side is exactly the desync the component's contract
    // forbids (see `SearchTopBar`).
    //
    // While the keyboard is up the platform eats the first press to put it away, so a focused
    // search costs one press before either handler sees anything. That is the system's back, not
    // the screen's, and is deliberately left alone.
    val clearSearchBackState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = clearSearchBackState,
        isBackEnabled = state.hasActiveSearch,
        onBackCancelled = {},
        onBackCompleted = { runAction(OnClearSearchAction) },
    )

    val dismissSearchFocusBackState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = dismissSearchFocusBackState,
        isBackEnabled = state.searchPhase == ExploreSearchPhase.FOCUS,
        onBackCancelled = {},
        onBackCompleted = { runAction(OnSearchDismissedAction) },
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SearchTopBar(
                model = state.searchTopBar,
                onEvent = { event ->
                    when (event) {
                        is SearchTopBarEvent.QueryChanged -> runAction(OnQueryChangeAction(newQuery = event.query))
                        SearchTopBarEvent.SearchActivated -> runAction(OnSearchActivatedAction)
                        SearchTopBarEvent.SearchDismissed -> runAction(OnSearchDismissedAction)
                        SearchTopBarEvent.SearchCleared -> runAction(OnClearSearchAction)
                        SearchTopBarEvent.ScanRequested -> onScanClick()
                    }
                },
            )
        },
    ) { padding ->
        if (isOnline.not()) {
            EmptyState(
                model = offlineEmptyStateUiModel(),
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

/**
 * Builds preview-only cover UI models off the composition, mirroring what `CoverModelsCollector`
 * does at runtime (component-contract.md § 7.2 R9) — a top-level `val`/`private fun`, never called
 * from inside a `@Composable`.
 */
private fun List<Book>.toPreviewCoverModels(
    variant: CoverVariant,
    surface: String? = null,
): Map<Int, CoverUiModel> = associate { book ->
    book.id to book.toCoverUiModel(
        variant = variant,
        sharedTransitionKey = bookCoverTransitionKey(
            editionId = book.currentEdition?.id,
            bookId = book.id,
            surface = surface,
        ),
    )
}

private val previewSearchFocusMoodTags = listOf(
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
)

private val previewRecentSearchQueries = listOf(
    "Bubblegum",
    "Earthlings",
    "Convenience Store",
    "Babel",
    "Piranesi",
)

private val previewMockState = ExploreScreenUiState(
    previousSearchQueries = previewRecentSearchQueries,
    recentSearchChips = RecentSearchChipsSnapshot(previousSearchQueries = previewRecentSearchQueries).compute().first,
    trendingBooks = ExploreMockData.trending,
    trendingCovers = ExploreMockData.trending.toPreviewCoverModels(
        CoverVariant.ExploreRail,
        SURFACE_TRENDING,
    ),
    loadingTrendingBooks = false,
    continueSeriesBooks = ExploreMockData.continueSeries,
    continueSeriesCovers = ExploreMockData.continueSeries.toPreviewCoverModels(
        CoverVariant.ExploreSeriesCard,
        SURFACE_UP_NEXT,
    ),
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
                trendingCovers = ExploreMockData.trending.toPreviewCoverModels(
                    CoverVariant.ExploreRail,
                    SURFACE_TRENDING,
                ),
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
                continueSeriesCovers = ExploreMockData.continueSeries.toPreviewCoverModels(
                    CoverVariant.ExploreSeriesCard,
                    SURFACE_UP_NEXT,
                ),
                loadingContinueSeriesBooks = false,
                previousSearchQueries = listOf("Bubblegum", "Earthlings"),
                recentSearchChips = RecentSearchChipsSnapshot(previousSearchQueries = listOf("Bubblegum", "Earthlings")).compute().first,
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
                trendingCovers = ExploreMockData.trending.toPreviewCoverModels(
                    CoverVariant.ExploreRail,
                    SURFACE_TRENDING,
                ),
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
                moodTags = previewSearchFocusMoodTags,
                moodChips = MoodChipsSnapshot(moodTags = previewSearchFocusMoodTags).compute().first,
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
    val queriedBooks = listOf(
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
    )

    SoftcoverTheme {
        ExploreScreenLayout(
            runAction = {},
            onBookClick = { _, _ -> },
            onScanClick = {},
            isOnline = true,
            state = ExploreScreenUiState(
                searchText = "Last to leave",
                queriedBooks = queriedBooks,
                queriedBookCovers = queriedBooks.toPreviewCoverModels(CoverVariant.ExploreSearchRow),
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
