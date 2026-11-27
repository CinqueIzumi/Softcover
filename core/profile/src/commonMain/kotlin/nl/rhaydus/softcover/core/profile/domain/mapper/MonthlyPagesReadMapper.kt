package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.MonthlyPages

private const val MONTHS_IN_YEAR = 12

/**
 * Sums the pages read per month from [updates], which must be in the order they were logged, bucketed
 * by the local date in [timeZone]. The unlogged remainders of finished and Did not finish books count
 * too; see [attributePages].
 */
internal fun toMonthlyPages(
    updates: List<ProgressUpdate>,
    readBooks: List<Book>,
    didNotFinishBooks: List<Book>,
    range: StatsRange,
    timeZone: TimeZone,
): MonthlyPages {
    val pagesPerMonth = attributePages(
        updates = updates,
        readBooks = readBooks,
        didNotFinishBooks = didNotFinishBooks,
        timeZone = timeZone,
    )
        .filter { it.date in range }
        .groupBy(
            { it.date.yearMonth },
            { it.pages },
        )
        .mapValues { (_, pages) -> pages.sum() }

    return when (range) {
        StatsRange.Unbounded -> MonthlyPages.AcrossYears(
            pages = (1..MONTHS_IN_YEAR).map { month ->
                pagesPerMonth.entries.filter { it.key.month.ordinal + 1 == month }.sumOf { it.value }
            },
        )

        is StatsRange.Bounded -> MonthlyPages.Span(
            months = generateSequence(range.start.yearMonth) {
                it.plus(
                    value = 1,
                    unit = DateTimeUnit.MONTH,
                )
            }
                .takeWhile { it <= range.endInclusive.yearMonth }
                .map { yearMonth ->
                    MonthCount(
                        year = yearMonth.year,
                        month = yearMonth.month.ordinal + 1,
                        count = pagesPerMonth[yearMonth] ?: 0,
                    )
                }
                .toList(),
        )
    }
}
