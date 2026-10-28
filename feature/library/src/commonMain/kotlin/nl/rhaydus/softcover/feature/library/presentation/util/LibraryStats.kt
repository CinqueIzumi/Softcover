package nl.rhaydus.softcover.feature.library.presentation.util

import nl.rhaydus.softcover.core.domain.model.Book

private fun Book.pagesForStats(): Int = currentEdition?.pages ?: defaultEdition?.pages ?: 0

internal fun List<Book>.totalPages(): Int = sumOf { it.pagesForStats() }

internal fun formatBookCount(count: Int): String = when (count) {
    1 -> "1 title"
    else -> "$count titles"
}

internal fun Book.finishedYear(): Int? = userBook?.finishDate?.year

internal fun List<Book>.availableFinishedYears(): List<Int> =
    mapNotNull { it.finishedYear() }
        .distinct()
        .sortedDescending()
