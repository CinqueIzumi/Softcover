package nl.rhaydus.softcover.core.component.chart

import kotlinx.datetime.LocalDate

sealed interface YearGridEvent {
    data class DayTapped(val date: LocalDate) : YearGridEvent
}
