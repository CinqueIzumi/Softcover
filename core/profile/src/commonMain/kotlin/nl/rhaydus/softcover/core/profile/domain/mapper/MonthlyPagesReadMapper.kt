package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.MonthlyPages

private const val MONTHS_IN_YEAR = 12

/**
 * Sums the pages read per month from [updates], which must be in the order they were logged, bucketed
 * by the local date in [timeZone]. A read book finished in [range] whose last recorded position falls
 * short of its page count adds the remainder on its finish date, since the final stretch is often never
 * logged; a read book with no updates adds all of its pages there.
 */
internal fun toMonthlyPages(
    updates: List<ProgressUpdate>,
    readBooks: List<Book>,
    range: StatsRange,
    timeZone: TimeZone,
): MonthlyPages {
    val dated = updates.map { it to it.at.toLocalDateTime(timeZone).date }

    val pagesPerMonth = mutableMapOf<YearMonth, Int>()

    fun add(
        date: LocalDate,
        pages: Int,
    ) {
        if (pages > 0 && date in range) {
            pagesPerMonth[date.yearMonth] = (pagesPerMonth[date.yearMonth] ?: 0) + pages
        }
    }

    dated.forEach { (update, date) ->
        add(
            date = date,
            pages = update.pagesRead,
        )
    }

    val datedByBook = dated.groupBy { it.first.bookId }

    readBooks.forEach { book ->
        val finishDate = book.userBook?.finishDate ?: return@forEach

        val lastPosition = datedByBook[book.id].orEmpty()
            .lastOrNull { (update, date) -> date <= finishDate && update.pages != null }
            ?.first?.pages ?: 0

        add(
            date = finishDate,
            pages = book.statsPages() - lastPosition,
        )
    }

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
