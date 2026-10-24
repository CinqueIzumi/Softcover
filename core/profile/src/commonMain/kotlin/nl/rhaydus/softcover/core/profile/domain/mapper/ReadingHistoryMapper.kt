package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.RecentlyLovedBook
import nl.rhaydus.softcover.core.profile.domain.model.YearCount

private const val MONTHS_IN_YEAR = 12
private const val RECENTLY_LOVED_COUNT = 3
private const val LOVED_RATING_FLOOR = 4.0

/**
 * Buckets every finished book by its finish date, so a re-read counts once, in its latest year.
 * Books without a finish date appear nowhere in the history.
 */
internal fun List<Book>.toReadingHistory(today: LocalDate): ReadingHistory {
    val dated = mapNotNull { book ->
        book.userBook?.finishDate?.let {
            DatedBook(
                book = book,
                finishDate = it,
            )
        }
    }
    val years = dated.map { it.finishDate.year }.distinct().sortedDescending()
    val booksPerYear = dated.groupingBy { it.finishDate.year }.eachCount()
    val pagesPerYear = dated.groupBy { it.finishDate.year }
        .mapValues { (_, books) -> books.sumOf { it.book.statsPages() } }

    val yearSpan = if (years.isEmpty()) {
        IntRange.EMPTY
    } else {
        years.last()..maxOf(
            today.year,
            years.first(),
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
        pagesByMonth = dated.toPagesByMonth(year = today.year),
        recentlyLoved = dated.toRecentlyLoved(),
    )
}

private data class DatedBook(
    val book: Book,
    val finishDate: LocalDate,
)

private fun List<DatedBook>.toPagesByMonth(year: Int): List<MonthCount> {
    val pagesPerMonth = filter { it.finishDate.year == year }
        .groupBy { it.finishDate.month.ordinal + 1 }
        .mapValues { (_, books) -> books.sumOf { it.book.statsPages() } }

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
