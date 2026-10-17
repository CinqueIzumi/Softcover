package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.share.ShareCardSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetModel

/**
 * The share sheet for one card: the [sheet] the component renders, and the [variantToggle] when more
 * than one card is on offer. [options] is the row of chips under the preview: full text while the card
 * has a description or review to expand, and the tag disclosure when it has tags to choose from. The
 * [tagPicker] groups show under that row while expanded.
 */
internal data class ShareSheetUiModel(
    val sheet: ShareCardSheetUiModel,
    val variantToggle: SegmentedControlUiModel<ShareCardVariant>?,
    val options: ChipSet<ShareSheetOption>,
    val tagPicker: ShareTagPickerUiModel?,
) : SheetModel<ShareSheetUiModel> {
    override val closeRequested: Boolean get() = sheet.closeRequested

    override fun requestingClose(): ShareSheetUiModel = copy(sheet = sheet.requestingClose())
}
