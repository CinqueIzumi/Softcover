package nl.rhaydus.softcover.core.component.quote

import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit

/**
 * Everything the reader can do in [QuoteSheet]. Every draft edit leaves as an event and returns as a new
 * model; the sheet keeps none of it.
 */
sealed interface QuoteSheetEvent {
    data class TextChanged(val text: String) : QuoteSheetEvent

    data class PositionTabSelected(val tab: QuotePositionTab) : QuoteSheetEvent

    /** The reader edited the Page or Percentage field. */
    data class PositionChanged(val text: String) : QuoteSheetEvent

    data class TimeChanged(
        val unit: ProgressTimeUnit,
        val text: String,
    ) : QuoteSheetEvent

    data class ProgressAdvanceToggled(val enabled: Boolean) : QuoteSheetEvent

    data class PrivacySelected(val option: QuotePrivacyOption) : QuoteSheetEvent

    /** See [nl.rhaydus.softcover.core.component.progress.WhenReadEvent.Shifted]. */
    data class WhenReadShifted(val minutes: Int) : QuoteSheetEvent

    data object WhenReadReset : QuoteSheetEvent

    /** The sheet dismisses itself right after emitting this; the host only records the quote. */
    data object SaveRequested : QuoteSheetEvent

    data object Dismissed : QuoteSheetEvent
}
