package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout

@Composable
internal fun LibraryLoadingGrid(
    layout: LibraryGridLayout,
    gridState: LazyGridState,
    columnsOverride: GridCells?,
) {
    val standInCount = if (columnsOverride != null) {
        12
    } else {
        when (layout) {
            LibraryGridLayout.GRID_TWO_COLUMNS -> 6
            LibraryGridLayout.GRID_THREE_COLUMNS -> 9
            LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
            LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
                -> 12

            LibraryGridLayout.LIST_LARGE -> 6
            LibraryGridLayout.LIST_COMPACT -> 10
        }
    }

    LayoutGrid(
        layout = layout,
        gridState = gridState,
        columnsOverride = columnsOverride,
    ) {
        items(
            count = standInCount,
            key = { index -> "stand-in-$index" },
        ) {
            BookCard(
                model = layout.standInCard(),
                onEvent = {},
            )
        }
    }
}
