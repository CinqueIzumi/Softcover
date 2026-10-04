package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.IndicatorBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

// File-level so scroll position survives recomposition when the shared tab body movableContent moves
// between the compact/rail/sidebar chrome layouts on a window resize.
private val editorialScrollState = ScrollState(initial = 0)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun EditorialContent(
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
                .verticalScroll(editorialScrollState)
                .padding(
                    top = 8.dp,
                    bottom = rememberBottomBarPadding(),
                ),
            verticalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            FeaturedSection(
                book = state.featuredUpcomingRelease,
                card = state.featuredCard,
                onBookClick = onBookClick,
                runAction = runAction,
            )

            ContinueSeriesSection(
                set = state.upNext,
                menuSheets = state.continueSeriesMenuSheets,
                header = state.upNextHeader,
                onBookClick = onBookClick,
                runAction = runAction,
            )

            BecauseYouReadSection(
                genre = state.becauseYouReadGenre,
                header = state.becauseYouReadHeader,
                genreOptions = state.becauseYouReadGenreOptions,
                genreSheet = state.becauseYouReadGenreSheet,
                genreGroup = state.becauseYouReadGenreGroup,
                set = state.becauseYouRead,
                isLoading = state.loadingBecauseYouReadBooks && state.becauseYouReadBooks.isEmpty(),
                onBookClick = onBookClick,
                runAction = runAction,
            )

            TrendingSection(
                set = state.trending,
                header = state.trendingHeader,
                onBookClick = onBookClick,
            )

            MoodGrid(
                tiles = state.moodTiles,
                header = state.moodGridHeader,
                runAction = runAction,
            )

            RecentSearchesSection(
                chips = state.recentSearchChips,
                runAction = runAction,
            )
        }
    }
}
