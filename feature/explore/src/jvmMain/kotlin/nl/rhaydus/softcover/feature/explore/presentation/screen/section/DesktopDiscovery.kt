package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

private val discoveryScrollState = ScrollState(initial = 0)

@Composable
internal fun DesktopDiscovery(
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
                cover = state.featuredCover,
                releaseBadge = state.featuredReleaseBadge,
                isLoading = state.loadingFeaturedUpcomingRelease && state.featuredUpcomingRelease == null,
                onBookClick = onBookClick,
                runAction = runAction,
            )

            DesktopUpNextSection(
                books = state.continueSeriesBooks,
                covers = state.continueSeriesCovers,
                unreleasedBadges = state.unreleasedBadges,
                isLoading = state.loadingContinueSeriesBooks && state.continueSeriesBooks.isEmpty(),
                onBookClick = onBookClick,
                runAction = runAction,
            )

            DesktopBecauseYouReadSection(
                genre = state.becauseYouReadGenre,
                genreOptions = state.becauseYouReadGenreOptions,
                books = state.becauseYouReadBooks,
                covers = state.becauseYouReadCovers,
                unreleasedBadges = state.unreleasedBadges,
                isLoading = state.loadingBecauseYouReadBooks && state.becauseYouReadBooks.isEmpty(),
                onBookClick = onBookClick,
                runAction = runAction,
            )

            DesktopTrendingSection(
                books = state.trendingBooks,
                covers = state.trendingCovers,
                unreleasedBadges = state.unreleasedBadges,
                isLoading = state.loadingTrendingBooks && state.trendingBooks.isEmpty(),
                onBookClick = onBookClick,
            )

            MoodGrid(
                moods = state.moodTags,
                isLoading = state.loadingMoodTags && state.moodTags.isEmpty(),
                runAction = runAction,
            )

            RecentSearchesSection(
                chips = state.recentSearchChips,
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
