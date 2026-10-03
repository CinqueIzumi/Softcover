package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout

/**
 * Renders [card] only when it [fits] [layout]: the layout flips at once while `BookCardModelsCollector`
 * recomputes cards asynchronously, so a stale card would draw in the wrong layout. Otherwise a masked
 * stand-in of the layout's own shape keeps the cell's footprint and [modifier].
 */
@Composable
internal fun LibraryBookCard(
    card: BookCardUiModel?,
    layout: LibraryGridLayout,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null,
) {
    if (card != null && card.fits(layout)) {
        BookCard(
            model = card,
            onEvent = onEvent,
            modifier = modifier,
            content = content,
        )
        return
    }

    BookCard(
        model = layout.standInCard(),
        onEvent = onEvent,
        modifier = modifier,
        isLoading = true,
    )
}

internal fun BookCardUiModel.fits(layout: LibraryGridLayout): Boolean =
    when (val card = variant) {
        is BookCardVariant.CoverOnly ->
            layout == LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY ||
                layout == LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY

        is BookCardVariant.Column ->
            layout == LibraryGridLayout.GRID_TWO_COLUMNS ||
                layout == LibraryGridLayout.GRID_THREE_COLUMNS

        is BookCardVariant.Row -> when (card.size) {
            BookCardVariant.Row.Size.Compact -> layout == LibraryGridLayout.LIST_COMPACT
            BookCardVariant.Row.Size.Standard -> false
            BookCardVariant.Row.Size.Large -> layout == LibraryGridLayout.LIST_LARGE
        }
    }
