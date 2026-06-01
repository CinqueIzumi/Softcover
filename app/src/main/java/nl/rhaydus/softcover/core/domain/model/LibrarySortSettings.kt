package nl.rhaydus.softcover.core.domain.model

data class LibrarySortSettings(
    val mode: LibrarySortMode,
    val direction: SortDirection,
) {
    companion object {
        fun defaultFor(mode: LibrarySortMode): LibrarySortSettings = LibrarySortSettings(
            mode = mode,
            direction = mode.defaultDirection,
        )
    }
}
