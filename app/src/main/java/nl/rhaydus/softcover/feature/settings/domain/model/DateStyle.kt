package nl.rhaydus.softcover.feature.settings.domain.model

enum class DateStyle(val label: String) {
    DAY_MONTH_YEAR(label = "DD/MM/YYYY"),
    MONTH_DAY_YEAR(label = "MM/DD/YYYY"),
    YEAR_MONTH_DAY(label = "YYYY/MM/DD"),
}