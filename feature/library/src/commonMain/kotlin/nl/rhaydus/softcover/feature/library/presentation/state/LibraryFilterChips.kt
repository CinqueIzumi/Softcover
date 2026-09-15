package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.chip.ChipUiModel

/**
 * The Filter sheet's facet chip rows for one tab, mapped by `FilterChipModelsCollector`
 * (`component-contract.md` § 7.2 R9) off [LibraryUiState.filterOptionsByTab]. Mirrors
 * [LibraryFilterOptions]'s facet shape one-for-one, so a facet with no available values is simply an
 * empty list here too.
 *
 * None of these chips carry [ChipUiModel.selected] — the Filter sheet holds its own local draft
 * ([LibraryFilters]) rather than committing a `LibraryAction` per tap, so which chip reads selected
 * depends on that ephemeral, composition-local draft rather than on anything in [LibraryUiState]. The
 * render resolves the current selection against [LibraryUiState.filterValueByChipKey] and the draft.
 */
internal data class LibraryFilterChips(
    val ownershipChips: List<ChipUiModel> = emptyList(),
    val formatChips: List<ChipUiModel> = emptyList(),
    val releaseYearChips: List<ChipUiModel> = emptyList(),
    val readYearChips: List<ChipUiModel> = emptyList(),
    val tagChips: List<ChipUiModel> = emptyList(),
    val ratingChips: List<ChipUiModel> = emptyList(),
) {
    val isEmpty: Boolean
        get() = ownershipChips.isEmpty() &&
            formatChips.isEmpty() &&
            releaseYearChips.isEmpty() &&
            readYearChips.isEmpty() &&
            tagChips.isEmpty() &&
            ratingChips.isEmpty()
}
