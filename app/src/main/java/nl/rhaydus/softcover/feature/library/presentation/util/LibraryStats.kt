package nl.rhaydus.softcover.feature.library.presentation.util

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.enum.JournalEventType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

private fun Book.pagesForStats(): Int = currentEdition?.pages ?: defaultEdition?.pages ?: 0

fun List<Book>.totalPages(): Int = sumOf { it.pagesForStats() }

fun formatBookCount(count: Int): String = when (count) {
    1 -> "1 title"
    else -> "$count titles"
}

fun formatPageCount(pages: Int): String? = if (pages > 0) {
    "%,d pages".format(pages)
} else {
    null
}

fun Book.finishedYear(): Int? {
    userBookRead?.finishedAt?.let { finishedAt ->
        runCatching { LocalDate.parse(finishedAt).year }
            .getOrNull()
            ?.let { return it }
    }

    val updatedAt = userBook?.journals
        ?.filter { it.event == JournalEventType.StatusFinished.eventName }
        ?.maxByOrNull { it.updatedAt }
        ?.updatedAt ?: return null

    return try {
        LocalDateTime.parse(updatedAt).year
    } catch (_: DateTimeParseException) {
        null
    }
}

fun List<Book>.availableFinishedYears(): List<Int> =
    mapNotNull { it.finishedYear() }
        .distinct()
        .sortedDescending()
