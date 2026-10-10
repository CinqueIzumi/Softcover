package nl.rhaydus.softcover.feature.profile.presentation.state

import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.share.ReadingLifeShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.core.component.share.ShareCardSheetUiModel
import nl.rhaydus.softcover.core.component.share.withShareCapture

internal fun profileShareSheetFor(
    card: ReadingLifeShareCardUiModel,
    progress: ShareCaptureProgress,
    closeRequested: Boolean,
): ShareCardSheetUiModel =
    ShareCardSheetUiModel(
        card = card,
        scaffold = profileShareSheetScaffold().withShareCapture(
            progress = progress,
            closeRequested = closeRequested,
        ),
        displayName = "${card.readerName}-reading-life",
        previewMaxWidth = 240.dp,
        previewMaxHeight = 420.dp,
    )

/** Records [capture] and rebuilds the held share sheet's scaffold to match. */
internal fun ProfileUiState.withShareCapture(capture: ShareCaptureState): ProfileUiState =
    copy(
        shareCapture = capture,
        shareSheet = shareSheet?.let { held ->
            held.copy(
                scaffold = held.scaffold.withShareCapture(capture),
            )
        },
    )
