package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.state.Skeleton
import nl.rhaydus.softcover.core.component.state.SkeletonUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnAddBookToLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveBookFromLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_FEATURED

@Composable
internal fun FeaturedSection(
    book: Book?,
    cover: CoverUiModel?,
    releaseBadge: BadgeUiModel?,
    header: SectionHeaderUiModel,
    skeleton: SkeletonUiModel,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    // Terminal empty (no upcoming release to feature) collapses away with no reserved space;
    // every other state - loading or loaded - keeps the card's own footprint so the crossfade
    // below never resizes the feed around it.
    if (isLoading.not() && book == null) return

    // No section header above this one, unlike every rail below it: the card names itself
    // with an inline SectionHeader on its own top row (see FeaturedCard), which keeps the feed's
    // opening screen for the book rather than for a header introducing it.
    SkeletonCrossfade(
        isLoading = isLoading,
        modifier = Modifier.padding(horizontal = 16.dp),
        label = "FeaturedSection",
    ) { loading ->
        if (loading) {
            Skeleton(model = skeleton)
        } else if (book != null) {
            FeaturedCard(
                book = book,
                cover = cover,
                releaseBadge = releaseBadge,
                header = header,
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
