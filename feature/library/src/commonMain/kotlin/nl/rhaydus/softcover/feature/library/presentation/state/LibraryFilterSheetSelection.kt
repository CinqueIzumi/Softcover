package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/**
 * The Filter sheet's facet chip rows for the open draft, mapped by `FilterDraftChipsCollector`
 * (`component-contract.md` § 7.2 R9) off [LibraryFilterChips] and [LibraryFilterDraft] — each chip's
 * [ChipUiModel.selected] resolved against the draft, and [tagChips] narrowed to the draft's
 * [LibraryFilterDraft.tagSearch]. [resultCount] and [clearAllEnabled] ride along so the footer never
 * derives them from the draft in composition either. The `show*` flags and [isEmpty] carry the
 * section visibility and empty-state decision, taken from [LibraryFilterChips] rather than from the
 * (search-narrowed) chip lists.
 */
internal data class LibraryFilterSheetSelection(
    val ownershipChips: ImmutableList<ChipUiModel> = persistentListOf(),
    val formatChips: ImmutableList<ChipUiModel> = persistentListOf(),
    val releaseYearChips: ImmutableList<ChipUiModel> = persistentListOf(),
    val readYearChips: ImmutableList<ChipUiModel> = persistentListOf(),
    val tagChips: ImmutableList<ChipUiModel> = persistentListOf(),
    val ratingChips: ImmutableList<ChipUiModel> = persistentListOf(),
    val resultCount: Int = 0,
    val clearAllEnabled: Boolean = false,
    val ownershipFormatHeader: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Ownership · Format"),
    val releaseYearHeader: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Release year"),
    val readYearHeader: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Year finished"),
    val tagsHeader: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Tags"),
    val ratingHeader: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Rating"),
    val tagsAvailableLabel: String = "0 available",
    val isEmpty: Boolean = false,
    val emptyTitle: String = "Nothing to filter yet",
    val emptyBody: String = "Too few books here to filter. Add a few more first.",
    val showOwnershipFormat: Boolean = false,
    val showReleaseYear: Boolean = false,
    val showReadYear: Boolean = false,
    val showTags: Boolean = false,
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
    draft: LibraryFilterDraft,
    resultCount: Int,
): LibraryFilterSheetSelection {
    fun ChipSet<LibraryFilterValue>.resolveSelection(): ImmutableList<ChipUiModel> = this.chips.map { chip ->
        val value = this[chip.key] ?: return@map chip

        chip.copy(selected = draft.filters.isSelected(value = value))
    }.toImmutableList()

    fun List<ChipUiModel>.matchingTagSearch(): ImmutableList<ChipUiModel> = filter { chip ->
        chip.label.contains(
            other = draft.tagSearch,
            ignoreCase = true,
        )
    }.toImmutableList()

    return LibraryFilterSheetSelection(
        ownershipChips = chips.ownershipChips.resolveSelection(),
        formatChips = chips.formatChips.resolveSelection(),
        releaseYearChips = chips.releaseYearChips.resolveSelection(),
        readYearChips = chips.readYearChips.resolveSelection(),
        tagChips = chips.tagChips.resolveSelection().matchingTagSearch(),
        ratingChips = chips.ratingChips.resolveSelection(),
        resultCount = resultCount,
        clearAllEnabled = draft.filters.isEmpty.not(),
        tagsAvailableLabel = "${chips.tagChips.chips.size} available",
        isEmpty = chips.isEmpty,
        showOwnershipFormat = chips.ownershipChips.isEmpty.not() || chips.formatChips.isEmpty.not(),
        showReleaseYear = chips.releaseYearChips.isEmpty.not(),
        showReadYear = chips.readYearChips.isEmpty.not(),
        showTags = chips.tagChips.isEmpty.not(),
        showRating = chips.ratingChips.isEmpty.not(),
    )
}

private fun LibraryFilters.isSelected(value: LibraryFilterValue): Boolean = when (value) {
    is LibraryFilterValue.Tag -> value.tag.id in tags.mapTo(mutableSetOf()) { it.id }
    is LibraryFilterValue.Format -> value.value in formats
    is LibraryFilterValue.ReleaseYear -> value.year in releaseYears
    is LibraryFilterValue.ReadYear -> readYear == value.year
    is LibraryFilterValue.Owned -> owned == value.owned
    is LibraryFilterValue.RatingMin -> ratingMin == value.threshold
}
