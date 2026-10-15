package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipUiModel

/**
 * Chooses which of the [variant] card's candidate tags appear on it. [header] is the disclosure chip
 * for the sheet's option row and [groups] show only while [expanded].
 */
internal data class ShareTagPickerUiModel(
    val variant: ShareCardVariant,
    val header: ChipUiModel,
    val expanded: Boolean,
    val groups: ImmutableList<ShareTagGroup>,
)
