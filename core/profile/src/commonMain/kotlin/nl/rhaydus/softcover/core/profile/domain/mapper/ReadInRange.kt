package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.StatsRange

internal fun List<Book>.readInRange(
    range: StatsRange,
    updates: List<ProgressUpdate>,
    timeZone: TimeZone,
): List<Book> {
    val updateDaysByBook = updates.groupBy(
        { it.bookId },
        { it.at.toLocalDateTime(timeZone).date },
    )

    return distinctBy { it.id }
        .filter {
            it.wasReadIn(
                range,
                updateDaysByBook[it.id].orEmpty(),
            )
        }
}

private fun Book.wasReadIn(
    range: StatsRange,
    updateDays: List<LocalDate>,
): Boolean {
    if (range == StatsRange.Unbounded) return true

    val userBook = userBook ?: return false

    val readDates = userBook.reads.flatMap { listOf(it.startedAt, it.finishedAt) }.mapNotNull { it.toLocalDateOrNull() }

    return userBook.finishDate in range ||
        (userBook.status == BookStatus.DidNotFinish && userBook.stopDate in range) ||
        readDates.any { it in range } ||
        updateDays.any { it in range }
}

private operator fun StatsRange.contains(date: LocalDate?): Boolean = date != null && date in this
