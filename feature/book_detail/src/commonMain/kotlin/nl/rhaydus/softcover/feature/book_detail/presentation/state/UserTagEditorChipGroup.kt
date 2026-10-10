package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.chip.ChipUiModel

/**
 * One category's editable-tag chips in the `TagEditorBottomSheet` collection — a header label plus
 * the chip row beneath it, mirroring [TagCategoryChipGroup]'s read-only counterpart.
 */
internal data class UserTagEditorChipGroup(
    val label: String,
    val chips: List<ChipUiModel>,
)
