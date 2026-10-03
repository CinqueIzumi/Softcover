package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
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
    header: SectionHeaderUiModel,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    // Terminal empty (no upcoming release to feature) collapses away with no reserved space; loading
    // lays out the card's own footprint.
    if (isLoading.not() && book == null) return

    // No section header above this one, unlike every rail below it: the card names itself with an
    // inline SectionHeader on its own top row (see FeaturedCard).
    FeaturedCard(
        book = book,
        cover = cover,
        releaseBadge = releaseBadge,
        header = header,
        isLoading = isLoading,
        onClick = {
            if (book != null) {
                onBookClick(
                    book,
                    SURFACE_FEATURED,
                )
            }
        },
        onWantToReadClick = { book?.let { runAction(OnAddBookToLibraryClickAction(book = it)) } },
        onRemoveFromLibraryClick = { book?.let { runAction(OnRemoveBookFromLibraryClickAction(book = it)) } },
        modifier = Modifier.padding(horizontal = 24.dp),
    )
}
