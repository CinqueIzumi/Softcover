package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout

@Composable
internal fun LibraryLoadingGrid(
    layout: LibraryGridLayout,
    cards: ImmutableList<BookCardUiModel>,
    gridState: LazyGridState,
    columnsOverride: GridCells?,
) {
    LayoutGrid(
        layout = layout,
        gridState = gridState,
        columnsOverride = columnsOverride,
    ) {
        items(
            items = cards,
            key = { card -> card.key.lazyKey },
        ) { card ->
            BookCard(
                model = card,
                onEvent = {},
            )
        }
    }
}
