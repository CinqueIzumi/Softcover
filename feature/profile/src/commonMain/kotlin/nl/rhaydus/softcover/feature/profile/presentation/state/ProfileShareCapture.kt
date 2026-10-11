package nl.rhaydus.softcover.feature.profile.presentation.state

import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.share.ReadingLifeShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCardSheetUiModel
import nl.rhaydus.softcover.core.component.share.withShareCapture

internal fun profileShareSheetFor(
    card: ReadingLifeShareCardUiModel,
    progress: ShareCaptureProgress,
): ShareCardSheetUiModel =
    ShareCardSheetUiModel(
        card = card,
        scaffold = profileShareSheetScaffold().withShareCapture(progress = progress),
        displayName = "${card.readerName}-reading-life",
        previewMaxWidth = 240.dp,
        previewMaxHeight = 420.dp,
    )
