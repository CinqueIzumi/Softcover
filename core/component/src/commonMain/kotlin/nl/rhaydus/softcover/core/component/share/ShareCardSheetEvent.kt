package nl.rhaydus.softcover.core.component.share

sealed interface ShareCardSheetEvent {
    data object ShareStarted : ShareCardSheetEvent

    data object SaveStarted : ShareCardSheetEvent

    /** Fires when a capture ends, whether it succeeded, failed or was cancelled. */
    data object CaptureFinished : ShareCardSheetEvent

    /** The card was shared; the host should ask the sheet to close. */
    data object Shared : ShareCardSheetEvent

    data object Dismissed : ShareCardSheetEvent
}
