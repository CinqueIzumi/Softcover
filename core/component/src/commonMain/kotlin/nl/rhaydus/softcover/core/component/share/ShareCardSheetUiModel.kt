package nl.rhaydus.softcover.core.component.share

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

/**
 * One share sheet: the [card] to preview, the finished [scaffold] around it, and the [displayName]
 * its captured file is saved under. Build [scaffold] with [withShareCapture] so the footer labels
 * and running button agree.
 */
data class ShareCardSheetUiModel(
    val card: ShareCardUiModel,
    val scaffold: SheetScaffoldUiModel,
    val displayName: String,
    val previewMaxWidth: Dp = 280.dp,
    val previewMaxHeight: Dp = 360.dp,
) : SheetModel<ShareCardSheetUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): ShareCardSheetUiModel = copy(scaffold = scaffold.requestingClose())

    companion object : UiModelPreviews<ShareCardSheetUiModel> {
        private val scaffold = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = SectionHeaderUiModel.Section(
                    eyebrow = "Share",
                    headline = "We Have Always Lived in the Castle",
                ),
            ),
            dismissLabel = "Done",
        )

        private val card = ShareCardUiModel.previews.getValue("Book")

        override val previews: ImmutableMap<String, ShareCardSheetUiModel> = persistentMapOf(
            "Idle" to ShareCardSheetUiModel(
                card = card,
                scaffold = scaffold.withShareCapture(progress = ShareCaptureProgress.Idle),
                displayName = "we-have-always-lived-in-the-castle",
            ),
            "Sharing" to ShareCardSheetUiModel(
                card = card,
                scaffold = scaffold.withShareCapture(progress = ShareCaptureProgress.Sharing),
                displayName = "we-have-always-lived-in-the-castle",
            ),
        )
    }
}
