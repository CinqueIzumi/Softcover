package nl.rhaydus.softcover.core.component.verdict

import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel

/** Everything the reader can do in [VerdictSheet], behind a single `onEvent` lambda (R1). */
sealed interface VerdictSheetEvent {
    /** The reader tapped the primary action; carries the sheet's local draft, which is otherwise never reported. */
    data class Saved(
        val rating: Double?,
        val review: RichTextUiModel,
        val hasSpoilers: Boolean,
    ) : VerdictSheetEvent

    data object DeleteRequested : VerdictSheetEvent

    /** The top action, a tap outside the sheet, or back. */
    data object Dismissed : VerdictSheetEvent
}
