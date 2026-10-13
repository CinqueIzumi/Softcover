package nl.rhaydus.softcover.feature.library.presentation.state

/**
 * The Filter sheet's uncommitted draft, seeded from committed state when the sheet opens
 * ([nl.rhaydus.softcover.feature.library.presentation.action.OnFilterSheetExpandedChangeAction]),
 * mutated by chip taps, in-sheet "Clear all" and the tag search field, and committed by
 * [nl.rhaydus.softcover.feature.library.presentation.action.OnApplyFiltersAction]. Cleared on close
 * without applying.
 */
internal data class LibraryFilterDraft(
    val tabId: String,
    val filters: LibraryFilters = LibraryFilters(),
    val tagSearch: String = "",
)
