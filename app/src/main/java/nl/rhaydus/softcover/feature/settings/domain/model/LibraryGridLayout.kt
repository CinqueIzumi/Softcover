package nl.rhaydus.softcover.feature.settings.domain.model

enum class LibraryGridLayout(val label: String) {
    GRID_TWO_COLUMNS(label = "Grid - 2 per row"),
    GRID_THREE_COLUMNS(label = "Grid - 3 per row"),
    LIST_COMPACT(label = "List - compact"),
    LIST_LARGE(label = "List - large cover"),
}
