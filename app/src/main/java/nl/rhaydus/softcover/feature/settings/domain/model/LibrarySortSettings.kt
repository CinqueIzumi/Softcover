package nl.rhaydus.softcover.feature.settings.domain.model

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
