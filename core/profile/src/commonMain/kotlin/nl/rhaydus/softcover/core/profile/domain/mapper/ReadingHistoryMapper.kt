package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.RecentlyLovedBook
import nl.rhaydus.softcover.core.profile.domain.model.YearCount

private const val MONTHS_IN_YEAR = 12
private const val RECENTLY_LOVED_COUNT = 3
private const val LOVED_RATING_FLOOR = 4.0

/**
 * Buckets every finished book by its finish date, so a re-read counts once, in its latest year; books
 * without a finish date appear nowhere in the book counts. Pages come from [attributePages], so a
 * Did not finish book and an unfinished one count in the year they were read.
 */
internal fun toReadingHistory(
    updates: List<ProgressUpdate>,
    readBooks: List<Book>,
    didNotFinishBooks: List<Book>,
    today: LocalDate,
    timeZone: TimeZone,
): ReadingHistory {
    val dated = readBooks.mapNotNull { book ->
        book.userBook?.finishDate?.let {
            DatedBook(
                book = book,
                finishDate = it,
            )
        }
    }

    val years = dated.map { it.finishDate.year }.distinct().sortedDescending()
    val booksPerYear = dated.groupingBy { it.finishDate.year }.eachCount()

    val credits = attributePages(
        updates = updates,
        readBooks = readBooks,
        didNotFinishBooks = didNotFinishBooks,
        timeZone = timeZone,
    )

    val pagesPerYear = credits.groupBy(
        { it.date.year },
        { it.pages },
    ).mapValues { (_, pages) -> pages.sum() }

    val spanYears = years + pagesPerYear.keys

    val yearSpan = if (spanYears.isEmpty()) {
        IntRange.EMPTY
    } else {
        spanYears.min()..maxOf(
            today.year,
            spanYears.max(),
        )
    }

    return ReadingHistory(
        booksByYear = yearSpan.map {
            YearCount(
                year = it,
                count = booksPerYear[it] ?: 0,
            )
        },
        pagesByYear = yearSpan.map {
            YearCount(
                year = it,
                count = pagesPerYear[it] ?: 0,
            )
        },
        yearsRead = years,
        trackedYears = years.size,
        pagesByMonth = credits.toPagesByMonth(year = today.year),
        recentlyLoved = dated.toRecentlyLoved(),
    )
}

private data class DatedBook(
    val book: Book,
    val finishDate: LocalDate,
)

private fun List<AttributedPages>.toPagesByMonth(year: Int): List<MonthCount> {
    val pagesPerMonth = filter { it.date.year == year }
        .groupBy(
            { it.date.month.ordinal + 1 },
            { it.pages },
        )
        .mapValues { (_, pages) -> pages.sum() }

    return (1..MONTHS_IN_YEAR).map { month ->
        MonthCount(
            year = year,
            month = month,
            count = pagesPerMonth[month] ?: 0,
        )
    }
}

private fun List<DatedBook>.toRecentlyLoved(): List<RecentlyLovedBook> = mapNotNull { dated ->
    val rating = dated.book.userBook?.rating

    if (rating == null || rating < LOVED_RATING_FLOOR) return@mapNotNull null

    RecentlyLovedBook(
        bookId = dated.book.id,
        ratingStars = rating,
        finishedOn = dated.finishDate,
    )
}
    .sortedWith(compareByDescending<RecentlyLovedBook> { it.finishedOn }.thenByDescending { it.bookId })
    .take(RECENTLY_LOVED_COUNT)
