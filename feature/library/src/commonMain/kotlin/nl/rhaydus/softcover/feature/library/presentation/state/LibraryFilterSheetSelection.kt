package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.ChipVariant

/**
 * The Filter sheet's facet chip rows for the open draft, mapped by `FilterDraftChipsCollector`
 * (`component-contract.md` § 7.2 R9) off [LibraryFilterChips] and [LibraryFilterDraft] — each chip's
 * [ChipVariant.Tonal.selected] resolved against the draft, and [tagChips] narrowed to the draft's
 * [LibraryFilterDraft.tagSearch]. [resultCount] and [clearAllEnabled] ride along so the footer never
 * derives them from the draft in composition either.
 */
internal data class LibraryFilterSheetSelection(
    val ownershipChips: List<ChipUiModel> = emptyList(),
    val formatChips: List<ChipUiModel> = emptyList(),
    val releaseYearChips: List<ChipUiModel> = emptyList(),
    val readYearChips: List<ChipUiModel> = emptyList(),
    val tagChips: List<ChipUiModel> = emptyList(),
    val ratingChips: List<ChipUiModel> = emptyList(),
    val resultCount: Int = 0,
    val clearAllEnabled: Boolean = false,
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
    valueByChipKey: Map<String, LibraryFilterValue>,
    resultCount: Int,
): LibraryFilterSheetSelection {
    fun List<ChipUiModel>.resolveSelection(): List<ChipUiModel> = map { chip ->
        val value = valueByChipKey[chip.key] ?: return@map chip

        chip.copy(variant = ChipVariant.Tonal(selected = draft.filters.isSelected(value = value)))
    }

    fun List<ChipUiModel>.matchingTagSearch(): List<ChipUiModel> = filter { chip ->
        chip.label.contains(
            other = draft.tagSearch,
            ignoreCase = true,
        )
    }

    return LibraryFilterSheetSelection(
        ownershipChips = chips.ownershipChips.resolveSelection(),
        formatChips = chips.formatChips.resolveSelection(),
        releaseYearChips = chips.releaseYearChips.resolveSelection(),
        readYearChips = chips.readYearChips.resolveSelection(),
        tagChips = chips.tagChips.resolveSelection().matchingTagSearch(),
        ratingChips = chips.ratingChips.resolveSelection(),
        resultCount = resultCount,
        clearAllEnabled = draft.filters.isEmpty.not(),
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
