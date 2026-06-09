package nl.rhaydus.softcover.feature.library.presentation.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.designsystem.presentation.util.formatGroupedNumber
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
    } catch (_: IllegalArgumentException) {
        null
    }
}

internal fun List<Book>.availableFinishedYears(): List<Int> =
    mapNotNull { it.finishedYear() }
        .distinct()
        .sortedDescending()
