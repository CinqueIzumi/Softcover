package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.DatePrecision
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.profile.domain.model.ReadDuration

/** Counted reads, on any shelf, whose own finish date falls in [range]. */
internal fun List<Book>.toReadDurations(range: StatsRange): List<ReadDuration> = flatMap { book ->
    book.userBook?.reads.orEmpty().mapNotNull { read -> read.toReadDurationOrNull(book) }
}.filter { it.finishedOn in range }

/** Reads finished within [range] that [toReadDurations] leaves out. */
internal fun List<Book>.countExcludedReads(range: StatsRange): Int = sumOf { book ->
    book.userBook?.reads.orEmpty().count { read ->
        val finishedOn = read.finishedAt.toLocalDateOrNull()

        finishedOn != null && finishedOn in range && read.toReadDurationOrNull(book) == null
    }
}

private fun UserBookRead.toReadDurationOrNull(book: Book): ReadDuration? {
    val startedOn = startedAt.toLocalDateOrNull() ?: return null
    val finishedOn = finishedAt.toLocalDateOrNull() ?: return null

    if (finishedAtPrecision != DatePrecision.DAY || finishedOn < startedOn) return null

    return ReadDuration(
        bookId = book.id,
        readId = id,
        startedOn = startedOn,
        finishedOn = finishedOn,
        pages = editionPages?.takeIf { it > 0 } ?: book.knownPages(),
    )
}

private fun Book.knownPages(): Int? = (readerEdition?.pages ?: pages)?.takeIf { it > 0 }

private fun String?.toLocalDateOrNull(): LocalDate? = this?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
