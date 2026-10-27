package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDay
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDayBook

/**
 * Breaks [date] down by book from the same [attributePages] entries the day grid sums, so the books'
 * pages add up to that day's total. Positions and times come from the day's own updates; only updates
 * that added pages count, so seconds-only and backwards updates are ignored.
 */
internal fun toReadingDay(
    updates: List<ProgressUpdate>,
    readBooks: List<Book>,
    date: LocalDate,
    timeZone: TimeZone,
): ReadingDay {
    val booksById = readBooks.associateBy { it.id }
    val updatesByBook = updates.groupBy { it.bookId }

    val dayUpdatesByBook = updatesByBook.mapValues { (_, bookUpdates) ->
        bookUpdates.filter { it.at.toLocalDateTime(timeZone).date == date }
    }

    val counted = dayUpdatesByBook
        .mapValues { (_, bookUpdates) -> bookUpdates.filter { it.pagesRead > 0 } }
        .filterValues { it.isNotEmpty() }

    val entries = attributePages(
        updates = updates,
        readBooks = readBooks,
        timeZone = timeZone,
    )
        .filter { it.date == date }
        .groupBy { it.bookId }

    val books = entries.map { (bookId, bookEntries) ->
        val book = booksById[bookId]
        val bookCounted = counted[bookId].orEmpty()
        val hasRemainder = bookEntries.any { it.isRemainder }

        val fromPage = bookCounted.firstOrNull()
            ?.let { update -> update.pages?.let { (it - update.pagesRead).coerceAtLeast(0) } }
            ?: lastPositionOnOrBefore(
                updates = updatesByBook[bookId].orEmpty(),
                date = date,
                timeZone = timeZone,
            )

        val toPage = if (hasRemainder && book != null) {
            book.statsPages()
        } else {
            dayUpdatesByBook[bookId].orEmpty().lastOrNull { it.pages != null }?.pages ?: fromPage
        }

        val times = bookCounted.map { it.at.toLocalDateTime(timeZone).time }

        ReadingDayBook(
            bookId = bookId,
            fromPage = fromPage,
            toPage = toPage,
            updateCount = bookCounted.size,
            pages = bookEntries.sumOf { it.pages },
            finished = book?.userBook?.finishDate == date,
            firstUpdateAt = times.minOrNull(),
            lastUpdateAt = times.maxOrNull(),
        )
    }

    return ReadingDay(
        date = date,
        books = books.sortedWith(
            compareByDescending<ReadingDayBook> { it.pages }
                .thenBy(nullsLast()) { it.firstUpdateAt },
        ),
    )
}
