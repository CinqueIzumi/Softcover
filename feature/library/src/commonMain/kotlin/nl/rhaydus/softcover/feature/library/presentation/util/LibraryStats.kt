package nl.rhaydus.softcover.feature.library.presentation.util

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.JournalEventType
import nl.rhaydus.softcover.core.domain.util.utcJournalTimeToLocalDate

private fun Book.pagesForStats(): Int = currentEdition?.pages ?: defaultEdition?.pages ?: 0

internal fun List<Book>.totalPages(): Int = sumOf { it.pagesForStats() }

internal fun formatBookCount(count: Int): String = when (count) {
    1 -> "1 title"
    else -> "$count titles"
}

internal fun Book.finishedYear(): Int? {
    val finishedJournalYear = userBook?.journals
        ?.filter { it.event == JournalEventType.StatusFinished.eventName }
        ?.maxByOrNull { it.updatedAt }
        ?.updatedAt
        ?.let { utcJournalTimeToLocalDate(it)?.year }

    return userBookRead?.finishedAt.toDateYearOrNull()
        ?: userBook?.lastReadDate.toDateYearOrNull()
        ?: finishedJournalYear
        ?: userBook?.createdAt?.let { utcJournalTimeToLocalDate(it)?.year }
}

private fun String?.toDateYearOrNull(): Int? =
    this?.let { runCatching { LocalDate.parse(it).year }.getOrNull() }

internal fun List<Book>.availableFinishedYears(): List<Int> =
    mapNotNull { it.finishedYear() }
        .distinct()
        .sortedDescending()
