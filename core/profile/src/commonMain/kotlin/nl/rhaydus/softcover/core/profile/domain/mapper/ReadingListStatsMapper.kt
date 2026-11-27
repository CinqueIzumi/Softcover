package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.model.ReadingListStats

/** A book waited when no read on it began or ended on or before the day it was added. */
internal fun List<Book>.toReadingListStats(range: StatsRange): ReadingListStats {
    val waited: List<Pair<LocalDate, Book>> = mapNotNull { book ->
        val userBook = book.userBook ?: return@mapNotNull null

        val addedOn = runCatching { LocalDate.parse(userBook.dateAdded) }.getOrNull() ?: return@mapNotNull null

        val earliestRead = userBook.earliestReadDateOrNull()

        if (earliestRead != null && earliestRead <= addedOn) null else addedOn to book
    }

    val waits: List<Int> = waited.mapNotNull { (addedOn, book) ->
        val firstStart = book.userBook?.firstStartOrNull() ?: return@mapNotNull null

        val days = addedOn.daysUntil(firstStart)

        days.takeIf { firstStart in range }
    }

    return ReadingListStats(
        addedCount = waited.count { (addedOn, _) -> addedOn in range },
        waitDays = waits.medianOrNull(),
        waitedCount = waits.size,
    )
}
