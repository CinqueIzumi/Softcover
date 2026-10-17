package nl.rhaydus.softcover.core.component.dialog

import kotlinx.datetime.LocalDate

/** Everything the reader can do in [SoftcoverDatePickerDialog], behind a single `onEvent` lambda (R1). */
sealed interface DatePickerEvent {
    data class Confirmed(val date: LocalDate) : DatePickerEvent

    /** Cancel, a tap outside, back, or confirming with no day selected. */
    data object Dismissed : DatePickerEvent
}
