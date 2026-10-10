package nl.rhaydus.softcover.core.component.share

import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

/**
 * The capture progress and close request a host holds for one [ShareCardSheetContent], folded from
 * the events the component reports.
 */
data class ShareCaptureState(
    val progress: ShareCaptureProgress = ShareCaptureProgress.Idle,
    val closeRequested: Boolean = false,
) {
    /**
     * This state after [event]. [ShareCardSheetEvent.Dismissed] changes nothing: closing the sheet
     * is the host's own state, and it resets this one when it does.
     */
    fun reduce(event: ShareCardSheetEvent): ShareCaptureState =
        when (event) {
            ShareCardSheetEvent.ShareStarted -> copy(progress = ShareCaptureProgress.Sharing)

            ShareCardSheetEvent.SaveStarted -> copy(progress = ShareCaptureProgress.Saving)

            ShareCardSheetEvent.CaptureFinished -> copy(progress = ShareCaptureProgress.Idle)

            ShareCardSheetEvent.Shared -> copy(closeRequested = true)

            ShareCardSheetEvent.Dismissed -> this
        }
}

/** This scaffold with the share footer, busy and close state for [capture]. */
fun SheetScaffoldUiModel.withShareCapture(capture: ShareCaptureState): SheetScaffoldUiModel =
    withShareCapture(
        progress = capture.progress,
        closeRequested = capture.closeRequested,
    )
