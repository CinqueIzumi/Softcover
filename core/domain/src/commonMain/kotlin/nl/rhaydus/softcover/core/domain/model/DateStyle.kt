package nl.rhaydus.softcover.core.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char

enum class DateStyle(
    val label: String,
    val formatter: DateTimeFormat<LocalDate>,
) {
    DAY_MONTH_YEAR(
        label = "Day, month, year",
        formatter = LocalDate.Format {
            day()
            char('/')
            monthNumber()
            char('/')
            year()
        },
    ),
    MONTH_DAY_YEAR(
        label = "Month, day, year",
        formatter = LocalDate.Format {
            monthNumber()
            char('/')
            day()
            char('/')
            year()
        },
    ),
    YEAR_MONTH_DAY(
        label = "Year, month, day",
        formatter = LocalDate.Format {
            year()
            char('/')
            monthNumber()
            char('/')
            day()
        },
    ),
    ;

    fun format(date: LocalDate): String = formatter.format(date)
}
