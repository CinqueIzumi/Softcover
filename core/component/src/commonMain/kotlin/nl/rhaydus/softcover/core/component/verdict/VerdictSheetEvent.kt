package nl.rhaydus.softcover.core.component.verdict

/** Everything the reader can do in [VerdictSheet], behind a single `onEvent` lambda (R1). */
sealed interface VerdictSheetEvent {
    /** The reader tapped the primary action; the host saves the draft it already holds. */
    data object Saved : VerdictSheetEvent

    data object DeleteRequested : VerdictSheetEvent

    /** The top action, a tap outside the sheet, or back. */
    data object Dismissed : VerdictSheetEvent
}
