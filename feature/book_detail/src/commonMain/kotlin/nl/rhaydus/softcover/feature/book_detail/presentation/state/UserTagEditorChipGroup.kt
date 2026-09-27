package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.domain.model.TagCategory

/**
 * One category's editable-tag chips in the `TagEditorBottomSheet` collection — a header label plus
 * the chip row beneath it, mirroring [TagCategoryChipGroup]'s read-only counterpart.
 */
internal data class UserTagEditorChipGroup(
    val category: TagCategory,
    val chips: List<ChipUiModel>,
)
