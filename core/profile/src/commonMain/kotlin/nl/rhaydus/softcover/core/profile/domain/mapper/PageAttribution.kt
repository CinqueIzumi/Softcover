package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book

/**
 * The single place pages are credited to days, so the day, month and per-book views cannot disagree.
 * [updates] must be in the order they were logged; each adds its `pagesRead` on its local date in
 * [timeZone]. A read book with a finish date adds the remainder of its page count past its last recorded
 * position on that date, and a Did not finish book with a stop date and a known stop page adds the
 * remainder up to that page on that date, since the final stretch is often never logged. Entries with
 * no pages are dropped.
 */
internal fun attributePages(
    updates: List<ProgressUpdate>,
    readBooks: List<Book>,
    didNotFinishBooks: List<Book>,
    timeZone: TimeZone,
): List<AttributedPages> {
    val fromUpdates = updates.map { update ->
        AttributedPages(
            date = update.at.toLocalDateTime(timeZone).date,
            bookId = update.bookId,
            pages = update.pagesRead,
            kind = AttributedPagesKind.Update,
        )
    }

    val updatesByBook = updates.groupBy { it.bookId }

    val finishRemainders = readBooks.mapNotNull { book ->
        val finishDate = book.userBook?.finishDate ?: return@mapNotNull null

        val endPage = book.statsPages()

        AttributedPages(
            date = finishDate,
            bookId = book.id,
            pages = endPage - lastPositionOnOrBefore(
                updates = updatesByBook[book.id].orEmpty(),
                date = finishDate,
                timeZone = timeZone,
            ),
            kind = AttributedPagesKind.FinishRemainder(endPage = endPage),
        )
    }

    val stopRemainders = didNotFinishBooks.mapNotNull { book ->
        val stopDate = book.userBook?.stopDate ?: return@mapNotNull null

        val stopPage = book.toStopPoint().page ?: return@mapNotNull null

        AttributedPages(
            date = stopDate,
            bookId = book.id,
            pages = stopPage - lastPositionOnOrBefore(
                updates = updatesByBook[book.id].orEmpty(),
                date = stopDate,
                timeZone = timeZone,
            ),
            kind = AttributedPagesKind.StopRemainder(endPage = stopPage),
        )
    }

    return (fromUpdates + finishRemainders + stopRemainders).filter { it.pages > 0 }
}

/** The last recorded page of a book's [updates] on or before [date], or 0 when none carries one. */
internal fun lastPositionOnOrBefore(
    updates: List<ProgressUpdate>,
    date: LocalDate,
    timeZone: TimeZone,
): Int = updates
    .lastOrNull { it.pages != null && it.at.toLocalDateTime(timeZone).date <= date }
    ?.pages ?: 0
