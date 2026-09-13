package nl.rhaydus.softcover.core.component.progress

/**
 * Everything the reader can do in `UpdateProgressBottomSheet`, as one sealed type behind a single
 * `onEvent` lambda (R1) rather than six hoisted callbacks.
 *
 * Every submit event carries [PagesSubmitted.actionAt]-style backdating: the instant the reader
 * picked in the sheet's "When did you read this?" row, ISO-8601, or `null` for "just now" — in which
 * case the server stamps the mutation with its own time. The picked value travels with whichever tab
 * the reader submits from, so it is on each event rather than on the model.
 */
sealed interface ProgressSheetEvent {
    /** The reader switched entry unit. The host persists it as the book's last-used unit. */
    data class TabSelected(val tab: ProgressSheetTab) : ProgressSheetEvent

    data class PagesSubmitted(
        val page: String,
        val actionAt: String?,
    ) : ProgressSheetEvent

    data class PercentageSubmitted(
        val percentage: String,
        val actionAt: String?,
    ) : ProgressSheetEvent

    data class TimeSubmitted(
        val hours: String,
        val minutes: String,
        val seconds: String,
        val actionAt: String?,
    ) : ProgressSheetEvent

    /**
     * The sheet's own "Mark as read" action — the same transition the Shelve control's "Read" row
     * fires. The host is responsible for dismissing the sheet as well as recording the finish; the
     * sheet does not assume either.
     */
    data class MarkAsReadRequested(val actionAt: String?) : ProgressSheetEvent

    data object Dismissed : ProgressSheetEvent
}
