package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.domain.model.TagCategory

/**
 * One category's read-only community-tag chips (the "Tags" section, The Book lens) — a header label
 * plus the chip row beneath it. [category] stays a domain enum here (this is `UiState`, not a
 * `:core:component` UI model, so R4 doesn't apply) purely so the render can keep reading
 * `category.label` for the section header until it too moves onto this state.
 */
internal data class TagCategoryChipGroup(
    val category: TagCategory,
    val chips: List<ChipUiModel>,
)
