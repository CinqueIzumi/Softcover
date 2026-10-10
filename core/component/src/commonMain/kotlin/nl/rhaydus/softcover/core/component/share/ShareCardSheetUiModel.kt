package nl.rhaydus.softcover.core.component.share

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

/**
 * One share sheet: the [card] to preview, the finished [scaffold] around it, and the [displayName]
 * its captured file is saved under. Build [scaffold] with [withShareCapture] so the footer labels,
 * busy and close state agree.
 */
data class ShareCardSheetUiModel(
    val card: ShareCardUiModel,
    val scaffold: SheetScaffoldUiModel,
    val displayName: String,
    val previewMaxWidth: Dp = 280.dp,
    val previewMaxHeight: Dp = 360.dp,
)
