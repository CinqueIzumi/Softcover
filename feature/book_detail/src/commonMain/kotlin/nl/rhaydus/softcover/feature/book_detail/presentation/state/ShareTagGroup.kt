package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/** One category of the share-tag picker; each chip's key is the tag name. */
internal data class ShareTagGroup(
    val header: SectionHeaderUiModel,
    val tags: ChipSet<String>,
)
