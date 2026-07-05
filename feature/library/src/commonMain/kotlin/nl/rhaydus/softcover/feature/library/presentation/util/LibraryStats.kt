package nl.rhaydus.softcover.feature.library.presentation.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.JournalEventType

private fun Book.pagesForStats(): Int = currentEdition?.pages ?: defaultEdition?.pages ?: 0

internal fun List<Book>.totalPages(): Int = sumOf { it.pagesForStats() }

internal fun formatBookCount(count: Int): String = when (count) {
    1 -> "1 title"
    else -> "$count titles"
}

internal fun formatPageCount(pages: Int): String? = if (pages > 0) {
    "${formatGroupedNumber(pages)} pages"
} else {
    null
}

internal fun Book.finishedYear(): Int? {
    val finishedJournalUpdatedAt = userBook?.journals
        ?.filter { it.event == JournalEventType.StatusFinished.eventName }
        ?.maxByOrNull { it.updatedAt }
        ?.updatedAt

    val candidates = listOfNotNull(
        userBookRead?.finishedAt,
        userBook?.lastReadDate,
        finishedJournalUpdatedAt,
        userBook?.createdAt,
    )

    return candidates.firstNotNullOfOrNull { it.toYearOrNull() }
}

private fun String.toYearOrNull(): Int? =
    runCatching { LocalDate.parse(this).year }.getOrNull()
        ?: runCatching { LocalDateTime.parse(this).year }.getOrNull()

internal fun List<Book>.availableFinishedYears(): List<Int> =
    mapNotNull { it.finishedYear() }
        .distinct()
        .sortedDescending()
