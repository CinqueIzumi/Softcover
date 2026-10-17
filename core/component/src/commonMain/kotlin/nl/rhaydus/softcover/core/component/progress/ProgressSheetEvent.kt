package nl.rhaydus.softcover.core.component.progress

/**
 * Everything the reader can do in `UpdateProgressBottomSheet`, as one sealed type behind a single
 * `onEvent` lambda (R1) rather than six hoisted callbacks.
 *
 * The submit events carry no backdate: the "When did you read this?" row writes the host's `readAt`
 * through [WhenReadShifted] and [WhenReadReset], and the host reads it when it handles a submit.
 */
sealed interface ProgressSheetEvent {
    /** The reader switched entry unit. The host persists it as the book's last-used unit. */
    data class TabSelected(val tab: ProgressSheetTab) : ProgressSheetEvent

    data class PagesSubmitted(val page: String) : ProgressSheetEvent

    data class PercentageSubmitted(val percentage: String) : ProgressSheetEvent

    data class TimeSubmitted(
        val hours: String,
        val minutes: String,
        val seconds: String,
    ) : ProgressSheetEvent

    /** The reader edited one unit of the time entry; the host clamps it into the draft. */
    data class TimeChanged(
        val unit: ProgressTimeUnit,
        val text: String,
    ) : ProgressSheetEvent

    /** The reader pressed a minutes stepper; [delta] is -1 or +1. */
    data class MinutesStepped(val delta: Int) : ProgressSheetEvent

    /**
     * The sheet's own "Mark as read" action — the same transition the Shelve control's "Read" row
     * fires. The sheet dismisses itself right after emitting this; the host only records the finish.
     */
    data object MarkAsReadRequested : ProgressSheetEvent

    /**
     * The reader moved the backdate by [minutes] (negative is earlier). From "Just now" it starts at the
     * current moment; the host clamps the result so it is never in the future.
     */
    data class WhenReadShifted(val minutes: Int) : ProgressSheetEvent

    /** The reader dismissed the backdate; the host clears it back to "Just now". */
    data object WhenReadReset : ProgressSheetEvent

    data object Dismissed : ProgressSheetEvent
}
