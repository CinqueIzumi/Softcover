package nl.rhaydus.softcover.feature.library.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout

internal actual fun LibraryGridLayout.standInCount(): Int = when (this) {
    LibraryGridLayout.GRID_TWO_COLUMNS -> 6
    LibraryGridLayout.GRID_THREE_COLUMNS -> 9
    LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
    LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
        -> 12

    LibraryGridLayout.LIST_LARGE -> 6
    LibraryGridLayout.LIST_COMPACT -> 10
}
