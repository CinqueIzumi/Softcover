package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

@Composable
internal fun DiscoverySections(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
    rail: @Composable (ExploreRail, BookCardSet<Book>, (BookCardEvent) -> Unit) -> Unit,
) {
    FeaturedSection(
        book = state.featuredUpcomingRelease,
        card = state.featuredCard,
        onBookClick = onBookClick,
        runAction = runAction,
    )

    UpNextSection(
        set = state.upNext,
        menuSheets = state.continueSeriesMenuSheets,
        header = state.upNextHeader,
        onBookClick = onBookClick,
        runAction = runAction,
    ) { onCardEvent ->
        rail(
            ExploreRail.UP_NEXT,
            state.upNext,
            onCardEvent,
        )
    }

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
    ) { onCardEvent ->
        rail(
            ExploreRail.BECAUSE_YOU_READ,
            state.becauseYouRead,
            onCardEvent,
        )
    }

    TrendingSection(
        set = state.trending,
        header = state.trendingHeader,
        onBookClick = onBookClick,
    ) { onCardEvent ->
        rail(
            ExploreRail.TRENDING,
            state.trending,
            onCardEvent,
        )
    }

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
