package nl.rhaydus.softcover.core.component.share

import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

/** Which capture a share sheet is running; [Idle] when neither is. */
enum class ShareCaptureProgress {
    Idle,
    Sharing,
    Saving,
}

/**
 * This scaffold with the share footer for [progress], closing once [closeRequested]. Safe to apply
 * repeatedly: it overwrites the footer and close state.
 */
fun SheetScaffoldUiModel.withShareCapture(
    progress: ShareCaptureProgress,
    closeRequested: Boolean,
): SheetScaffoldUiModel =
    copy(
        footer = SheetFooterUiModel.Inline(
            primary = SheetFooterButton(
                label = if (progress == ShareCaptureProgress.Sharing) "Sharing…" else "Share",
                running = progress == ShareCaptureProgress.Sharing,
            ),
            secondary = SheetFooterButton(
                label = "Save to gallery",
                running = progress == ShareCaptureProgress.Saving,
            ),
        ),
        closeRequested = closeRequested,
    )
