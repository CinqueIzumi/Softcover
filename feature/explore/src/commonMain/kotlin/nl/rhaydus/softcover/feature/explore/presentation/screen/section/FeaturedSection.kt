package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnAddBookToLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveBookFromLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.model.FeaturedCardEvent
import nl.rhaydus.softcover.feature.explore.presentation.model.FeaturedCardUiModel
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_FEATURED

@Composable
internal fun FeaturedSection(
    book: Book?,
    card: FeaturedCardUiModel?,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    if (card == null) return

    FeaturedCard(
        model = card,
        onEvent = { event ->
            // A loading card has no book, so its events resolve to nothing.
            book?.let {
                when (event) {
                    FeaturedCardEvent.Clicked -> onBookClick(
                        it,
                        SURFACE_FEATURED,
                    )

                    FeaturedCardEvent.WantToReadClicked -> runAction(OnAddBookToLibraryClickAction(book = it))

                    FeaturedCardEvent.RemoveFromLibraryClicked -> runAction(
                        OnRemoveBookFromLibraryClickAction(book = it),
                    )
                }
            }
        },
        modifier = Modifier.padding(horizontal = Spacing.xl),
    )
}
