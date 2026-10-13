package nl.rhaydus.softcover.core.uibinding.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

/**
 * "September 2, 2026".
 */
fun LocalDate.formatLongDate(): String {
    val formatter = LocalDate.Format {
        monthName(MonthNames.ENGLISH_FULL)
        char(' ')
        day(Padding.NONE)
        char(',')
        char(' ')
        year()
    }

    return formatter.format(this)
}
