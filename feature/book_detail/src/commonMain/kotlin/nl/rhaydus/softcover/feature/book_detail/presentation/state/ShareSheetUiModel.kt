package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.share.ShareCardUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

/**
 * The share sheet for one [card]: its [scaffold] header, the [displayName] its captured file is
 * saved under, and the [variantToggle] when more than one card is on offer.
 */
internal data class ShareSheetUiModel(
    val card: ShareCardUiModel,
    val scaffold: SheetScaffoldUiModel,
    val displayName: String,
    val variantToggle: SegmentedControlUiModel<ShareCardVariant>?,
)
