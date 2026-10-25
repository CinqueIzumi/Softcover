package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/**
 * The Filter sheet's facet chip rows for the open draft, mapped by `FilterDraftChipsCollector`
 * (`component-contract.md` § 7.2 R9) off [LibraryFilterChips], [LibraryFilterOptions] and
 * [LibraryFilterDraft] — each chip's [ChipUiModel.selected] resolved against the draft, the tag
 * sections as one [TagSourceSummary] per source.
 * [resultCount] and [clearAllEnabled] ride along so the footer never derives them from the draft in
 * composition either. The `show*` flags and [isEmpty] carry the section visibility and empty-state
 * decision; a source with no tags on the shelf has a null summary.
 */
internal data class LibraryFilterSheetSelection(
    val ownershipChips: ImmutableList<ChipUiModel> = persistentListOf(),
    val formatChips: ImmutableList<ChipUiModel> = persistentListOf(),
    val releaseYearChips: ImmutableList<ChipUiModel> = persistentListOf(),
    val readYearChips: ImmutableList<ChipUiModel> = persistentListOf(),
    val yourTags: TagSourceSummary? = null,
    val hardcoverTags: TagSourceSummary? = null,
    val ratingChips: ImmutableList<ChipUiModel> = persistentListOf(),
    val resultCount: Int = 0,
    val clearAllEnabled: Boolean = false,
    val ownershipFormatHeader: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Ownership · Format"),
    val releaseYearHeader: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Release year"),
    val readYearHeader: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Year finished"),
    val ratingHeader: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Rating"),
    val isEmpty: Boolean = false,
    val emptyTitle: String = "Nothing to filter yet",
    val emptyBody: String = "Too few books here to filter. Add a few more first.",
    val showOwnershipFormat: Boolean = false,
    val showReleaseYear: Boolean = false,
    val showReadYear: Boolean = false,
    val showRating: Boolean = false,
)

/**
 * Pure, dispatcher-friendly builder resolving [chips] against [draft] (`component-contract.md`
 * § 7.2 R9) — kept top-level, like [buildLibraryFilterChips], so `FilterDraftChipsCollector` can call
 * it on `Dispatchers.Default`. [resultCount] is passed in rather than computed here since it needs
 * [libraryPreviewCount]'s wider state slice (search query, sort, editions).
 */
internal fun buildLibraryFilterSheetSelection(
    chips: LibraryFilterChips,
    options: LibraryFilterOptions,
    draft: LibraryFilterDraft,
    resultCount: Int,
): LibraryFilterSheetSelection {
    fun ChipSet<LibraryFilterValue>.resolveSelection(): ImmutableList<ChipUiModel> = this.chips.map { chip ->
        val value = this[chip.key] ?: return@map chip

        chip.copy(selected = draft.filters.isSelected(value = value))
    }.toImmutableList()

    val yourTags = buildTagSourceSummary(
        source = TagSource.YOURS,
        options = options,
        filters = draft.filters,
        section = draft.yourTagsSection,
    )
    val hardcoverTags = buildTagSourceSummary(
        source = TagSource.HARDCOVER,
        options = options,
        filters = draft.filters,
        section = draft.hardcoverTagsSection,
    )

    return LibraryFilterSheetSelection(
        ownershipChips = chips.ownershipChips.resolveSelection(),
        formatChips = chips.formatChips.resolveSelection(),
        releaseYearChips = chips.releaseYearChips.resolveSelection(),
        readYearChips = chips.readYearChips.resolveSelection(),
        yourTags = yourTags,
        hardcoverTags = hardcoverTags,
        ratingChips = chips.ratingChips.resolveSelection(),
        resultCount = resultCount,
        clearAllEnabled = draft.filters.isEmpty.not(),
        isEmpty = chips.isEmpty && yourTags == null && hardcoverTags == null,
        showOwnershipFormat = chips.ownershipChips.isEmpty.not() || chips.formatChips.isEmpty.not(),
        showReleaseYear = chips.releaseYearChips.isEmpty.not(),
        showReadYear = chips.readYearChips.isEmpty.not(),
        showRating = chips.ratingChips.isEmpty.not(),
    )
}

private fun LibraryFilters.isSelected(value: LibraryFilterValue): Boolean = when (value) {
    is LibraryFilterValue.HardcoverTag -> hardcoverTags.contains(
        key = value.key,
        direction = value.direction,
    )

    is LibraryFilterValue.YourTag -> yourTags.contains(
        key = value.key,
        direction = value.direction,
    )

    is LibraryFilterValue.Format -> value.value in formats
    is LibraryFilterValue.ReleaseYear -> value.year in releaseYears
    is LibraryFilterValue.ReadYear -> readYear == value.year
    is LibraryFilterValue.Owned -> owned == value.owned
    is LibraryFilterValue.RatingMin -> ratingMin == value.threshold
}

/** Sets a source's search text in the built field, so the caret never waits on a collector pass. */
internal fun LibraryFilterSheetSelection.withSearchText(
    source: TagSource,
    text: String,
): LibraryFilterSheetSelection = when (source) {
    TagSource.YOURS -> copy(yourTags = yourTags?.withSearchText(text = text))
    TagSource.HARDCOVER -> copy(hardcoverTags = hardcoverTags?.withSearchText(text = text))
}

private fun TagSourceSummary.withSearchText(text: String): TagSourceSummary =
    copy(searchField = searchField.copy(text = text))
