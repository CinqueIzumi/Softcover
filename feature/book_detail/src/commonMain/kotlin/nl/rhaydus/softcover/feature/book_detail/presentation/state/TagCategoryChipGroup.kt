package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipUiModel

/** One category's read-only community-tag chips: a header label plus the chip row beneath it. */
internal data class TagCategoryChipGroup(
    val label: String,
    val chips: ImmutableList<ChipUiModel>,
)
