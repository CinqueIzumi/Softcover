package nl.rhaydus.softcover.core.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char

enum class DateStyle(
    val label: String,
    val formatter: DateTimeFormat<LocalDate>,
) {
    DAY_MONTH_YEAR(
        label = "DD/MM/YYYY",
        formatter = LocalDate.Format {
            dayOfMonth()
            char('/')
            monthNumber()
            char('/')
            year()
        },
    ),
    MONTH_DAY_YEAR(
        label = "MM/DD/YYYY",
        formatter = LocalDate.Format {
            monthNumber()
            char('/')
            dayOfMonth()
            char('/')
            year()
        },
    ),
    YEAR_MONTH_DAY(
        label = "YYYY/MM/DD",
        formatter = LocalDate.Format {
            year()
            char('/')
            monthNumber()
            char('/')
            dayOfMonth()
        },
    ),
}
