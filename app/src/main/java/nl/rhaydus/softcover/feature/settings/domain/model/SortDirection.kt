package nl.rhaydus.softcover.feature.settings.domain.model

enum class SortDirection(val label: String) {
    ASCENDING(label = "ascending"),
    DESCENDING(label = "descending"),
    ;

    fun flipped(): SortDirection = when (this) {
        ASCENDING -> DESCENDING
        DESCENDING -> ASCENDING
    }
}
