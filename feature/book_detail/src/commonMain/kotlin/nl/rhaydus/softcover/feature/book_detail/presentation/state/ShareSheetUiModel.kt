package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.share.ShareCardSheetUiModel

/**
 * The share sheet for one card: the [sheet] the component renders, and the [variantToggle] when more
 * than one card is on offer.
 */
internal data class ShareSheetUiModel(
    val sheet: ShareCardSheetUiModel,
    val variantToggle: SegmentedControlUiModel<ShareCardVariant>?,
)
