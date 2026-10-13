package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnAddBookToLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveBookFromLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_FEATURED
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

@Composable
internal fun DiscoverySections(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
    sectionGap: Dp,
    rail: @Composable (ExploreRail, BookCardSet<Book>, (BookCardEvent) -> Unit) -> Unit,
) {
    val featuredBook = state.featuredUpcomingRelease

    FeaturedSection(
        card = state.featuredCard,
        onOpen = {
            featuredBook?.let { book ->
                onBookClick(
                    book,
                    SURFACE_FEATURED,
                )
            }
        },
        onAddToLibrary = {
            featuredBook?.let { book ->
                runAction(OnAddBookToLibraryClickAction(book = book))
            }
        },
        onRemoveFromLibrary = {
            featuredBook?.let { book ->
                runAction(OnRemoveBookFromLibraryClickAction(book = book))
            }
        },
    )

    UpNextSection(
        set = state.upNext,
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

    Column {
        BecauseYouReadSection(
            header = state.becauseYouReadHeader,
            genreControl = state.becauseYouReadGenreControl,
            set = state.becauseYouRead,
            onBookClick = onBookClick,
            runAction = runAction,
            trailingGap = sectionGap,
        ) { onCardEvent ->
            rail(
                ExploreRail.BECAUSE_YOU_READ,
                state.becauseYouRead,
                onCardEvent,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(sectionGap)) {
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
    }
}
