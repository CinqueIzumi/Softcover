package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel

/**
 * The Filter sheet's facet chip rows for one tab, mapped by `FilterChipModelsCollector`
 * (`component-contract.md` § 7.2 R9) off [LibraryUiState.filterOptionsByTab]. Mirrors
 * [LibraryFilterOptions]'s facet shape one-for-one, so a facet with no available values is simply an
 * empty [ChipSet] here too.
 *
 * None of these chips carry [ChipUiModel.selected] — the Filter sheet holds its own local
 * draft ([LibraryFilters]) rather than committing a `LibraryAction` per tap, so which chip reads
 * selected depends on that ephemeral, composition-local draft rather than on anything in
 * [LibraryUiState]. The render resolves the current selection against [get] and the draft.
 */
internal data class LibraryFilterChips(
    val ownershipChips: ChipSet<LibraryFilterValue> = ChipSet(),
    val formatChips: ChipSet<LibraryFilterValue> = ChipSet(),
    val releaseYearChips: ChipSet<LibraryFilterValue> = ChipSet(),
    val readYearChips: ChipSet<LibraryFilterValue> = ChipSet(),
    val tagChips: ChipSet<LibraryFilterValue> = ChipSet(),
    val ratingChips: ChipSet<LibraryFilterValue> = ChipSet(),
) {
    val isEmpty: Boolean
        get() = ownershipChips.isEmpty &&
            formatChips.isEmpty &&
            releaseYearChips.isEmpty &&
            readYearChips.isEmpty &&
            tagChips.isEmpty &&
            ratingChips.isEmpty

    operator fun get(key: String): LibraryFilterValue? =
        ownershipChips[key]
            ?: formatChips[key]
            ?: releaseYearChips[key]
            ?: readYearChips[key]
            ?: tagChips[key]
            ?: ratingChips[key]
}
