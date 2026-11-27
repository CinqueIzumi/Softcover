package nl.rhaydus.softcover.core.profile.domain.model

/**
 * The reader's whole finishing history, independent of any stats range.
 *
 * [booksByYear] and [pagesByYear] run without gaps from the first year with a finished book or credited
 * pages to the current one, zero-filled, and are empty when there are neither. [pagesByYear] and
 * [pagesByMonth] count every credited page, including those of Did not finish books. [yearsRead] is
 * newest first. [pagesByMonth] covers every month of the current year.
 */
data class ReadingHistory(
    val booksByYear: List<YearCount> = emptyList(),
    val pagesByYear: List<YearCount> = emptyList(),
    val yearsRead: List<Int> = emptyList(),
    val trackedYears: Int = 0,
    val pagesByMonth: List<MonthCount> = emptyList(),
    val recentlyLoved: List<RecentlyLovedBook> = emptyList(),
)
