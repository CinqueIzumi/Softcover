package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnAddBookToLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveBookFromLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_FEATURED

@Composable
internal fun DesktopFeaturedSection(
    book: Book?,
    cover: CoverUiModel?,
    releaseBadge: BadgeUiModel?,
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
                cover = cover,
                releaseBadge = releaseBadge,
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
