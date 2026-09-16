package nl.rhaydus.softcover.core.uibinding.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import nl.rhaydus.common.currentLocalDate

/**
 * Two date formatters lifted out of `UnreleasedBadge` verbatim. They were named for releases only
 * because a release date was their first caller — [formatCompactDate] now also feeds
 * `UnreleasedMapper`'s badge copy, and [formatLongDate] the same, while the roadmap screen's "Last
 * updated" footer is the second, non-release consumer that motivated moving both here rather than
 * leaving them under a release-shaped name.
 */

/**
 * "Sep 2", with ", <year>" appended only when [LocalDate.year] is not the current year.
 */
fun LocalDate.formatCompactDate(): String {
    val formatter = LocalDate.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        day(Padding.NONE)
    }

    val base = formatter.format(this)
    val now = currentLocalDate()

    return if (year == now.year) base else "$base, $year"
}

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
