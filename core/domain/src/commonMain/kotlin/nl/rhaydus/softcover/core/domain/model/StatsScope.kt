package nl.rhaydus.softcover.core.domain.model

/** What the reader chose to look at; turn it into dates with [resolve]. */
sealed interface StatsScope {
    data class Rolling(val window: StatsWindow) : StatsScope

    data class CalendarYear(val year: Int) : StatsScope

    data object AllTime : StatsScope
}
