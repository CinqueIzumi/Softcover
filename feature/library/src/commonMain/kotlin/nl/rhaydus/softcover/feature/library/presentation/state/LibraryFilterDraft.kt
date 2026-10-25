package nl.rhaydus.softcover.feature.library.presentation.state

/**
 * The Filter sheet's uncommitted draft, seeded from committed state when the sheet opens
 * ([nl.rhaydus.softcover.feature.library.presentation.action.OnFilterSheetExpandedChangeAction]),
 * mutated by chip taps, in-sheet "Clear all" and the tag sections, and committed by
 * [nl.rhaydus.softcover.feature.library.presentation.action.OnApplyFiltersAction]. Cleared on close
 * without applying, section state with it.
 */
internal data class LibraryFilterDraft(
    val tabId: String,
    val filters: LibraryFilters = LibraryFilters(),
    val yourTagsSection: TagSectionDraft = TagSectionDraft(),
    val hardcoverTagsSection: TagSectionDraft = TagSectionDraft(),
) {
    fun sectionFor(source: TagSource): TagSectionDraft = when (source) {
        TagSource.YOURS -> yourTagsSection
        TagSource.HARDCOVER -> hardcoverTagsSection
    }

    fun withSection(
        source: TagSource,
        section: TagSectionDraft,
    ): LibraryFilterDraft = when (source) {
        TagSource.YOURS -> copy(yourTagsSection = section)
        TagSource.HARDCOVER -> copy(hardcoverTagsSection = section)
    }
}
