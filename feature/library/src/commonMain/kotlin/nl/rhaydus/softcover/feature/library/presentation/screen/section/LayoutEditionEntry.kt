package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.presentation.prefetch.prefetchBookDetailOnPress

@Composable
internal fun LayoutEditionEntry(
    edition: BookEdition,
    cover: CoverUiModel?,
    card: BookCardUiModel?,
    layout: LibraryGridLayout,
    onCardEvent: (BookCardEvent) -> Unit,
    onEditionClick: (BookEdition) -> Unit,
    modifier: Modifier = Modifier,
    dragHandle: (@Composable () -> Unit)? = null,
) {
    val entryModifier = modifier.prefetchBookDetailOnPress(edition.bookId)

    val title = edition.title.orEmpty()
    val authorName = edition.authors.map { it.name }.firstOrNull().orEmpty()

    when (layout) {
        LibraryGridLayout.GRID_TWO_COLUMNS,
        LibraryGridLayout.GRID_THREE_COLUMNS,
        LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
        LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            -> {
            if (card != null) {
                BookCard(
                    model = card,
                    onEvent = onCardEvent,
                    modifier = modifier,
                    content = dragHandle,
                )
            } else {
                // One frame before the collector lands: keep the cell's footprint and modifiers.
                Cover(
                    model = null,
                    modifier = modifier.fillMaxWidth(),
                )
            }
        }

        LibraryGridLayout.LIST_COMPACT -> {
            CompactRow(
                modifier = entryModifier,
                title = title,
                authorName = authorName,
                onClick = { onEditionClick(edition) },
                trailing = dragHandle,
            )
        }

        LibraryGridLayout.LIST_LARGE -> {
            LargeRow(
                modifier = entryModifier,
                title = title,
                authorName = authorName,
                onClick = { onEditionClick(edition) },
                trailing = dragHandle,
            ) { coverModifier ->
                Cover(
                   model = cover,
                    modifier = coverModifier,
                )
            }
        }
    }
}
