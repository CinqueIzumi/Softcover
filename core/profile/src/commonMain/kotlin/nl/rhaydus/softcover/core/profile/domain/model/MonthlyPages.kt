package nl.rhaydus.softcover.core.profile.domain.model

/** The pages read in each month, counted from progress updates on the day they were logged. */
sealed interface MonthlyPages {
    /** Every calendar month a bounded range touches, in order and zero-filled, so a partial month is still a column. */
    data class Span(val months: List<MonthCount>) : MonthlyPages

    /** Twelve totals, January first, each summing that month across every year. */
    data class AcrossYears(val pages: List<Int>) : MonthlyPages
}
