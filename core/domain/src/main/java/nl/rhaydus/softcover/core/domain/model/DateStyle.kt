package nl.rhaydus.softcover.core.domain.model

import java.time.format.DateTimeFormatter

enum class DateStyle(
    val label: String,
    val formatter: DateTimeFormatter,
) {
    DAY_MONTH_YEAR(
        label = "DD/MM/YYYY",
        formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"),
    ),
    MONTH_DAY_YEAR(
        label = "MM/DD/YYYY",
        formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy"),
    ),
    YEAR_MONTH_DAY(
        label = "YYYY/MM/DD",
        formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd"),
    ),
}
