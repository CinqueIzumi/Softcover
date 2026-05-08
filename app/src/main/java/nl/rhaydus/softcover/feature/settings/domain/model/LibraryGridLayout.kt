package nl.rhaydus.softcover.feature.settings.domain.model

enum class LibraryGridLayout(val label: String) {
    GRID_TWO_COLUMNS(label = "Grid - 2 per row, with details"),
    GRID_TWO_COLUMNS_COVER_ONLY(label = "Grid - 2 per row, covers only"),
    GRID_THREE_COLUMNS(label = "Grid - 3 per row, with details"),
    GRID_THREE_COLUMNS_COVER_ONLY(label = "Grid - 3 per row, covers only"),
    LIST_COMPACT(label = "List - compact"),
    LIST_LARGE(label = "List - large cover"),
}
