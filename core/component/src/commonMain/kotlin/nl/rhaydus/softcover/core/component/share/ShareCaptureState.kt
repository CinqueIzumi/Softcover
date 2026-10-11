package nl.rhaydus.softcover.core.component.share

import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

/**
 * The capture progress a host holds for one [ShareCardSheetContent], folded from the events the
 * component reports.
 */
data class ShareCaptureState(
    val progress: ShareCaptureProgress = ShareCaptureProgress.Idle,
) {
    fun reduce(event: ShareCardSheetEvent): ShareCaptureState =
        when (event) {
            ShareCardSheetEvent.ShareStarted -> copy(progress = ShareCaptureProgress.Sharing)

            ShareCardSheetEvent.SaveStarted -> copy(progress = ShareCaptureProgress.Saving)

            ShareCardSheetEvent.CaptureFinished -> copy(progress = ShareCaptureProgress.Idle)

            ShareCardSheetEvent.Shared,
            ShareCardSheetEvent.Dismissed,
            -> this
        }
}

fun SheetScaffoldUiModel.withShareCapture(capture: ShareCaptureState): SheetScaffoldUiModel =
    withShareCapture(progress = capture.progress)
