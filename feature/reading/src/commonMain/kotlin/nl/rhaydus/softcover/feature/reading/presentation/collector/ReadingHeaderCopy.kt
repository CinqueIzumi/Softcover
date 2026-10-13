package nl.rhaydus.softcover.feature.reading.presentation.collector

import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.softcover.core.domain.model.Book
import kotlin.math.roundToInt

internal fun List<Book>.averageProgress(): Float? {
    if (isEmpty()) return null
    val values = mapNotNull { it.userBookRead?.progress }
    if (values.isEmpty()) return null
    return values.average().toFloat()
}

internal fun mastheadEyebrowFor(isRefreshing: Boolean): String =
    if (isRefreshing) "Catching up on your reading…" else "Now reading"

internal fun greetingForNow(): String {
    val hour = currentLocalDateTime().hour
    return when (hour) {
        in 5..11 -> "Good morning."
        in 12..17 -> "Good afternoon."
        in 18..21 -> "Good evening."
        else -> "Still up?"
    }
}

internal fun buildSubtitle(
    bookCount: Int,
    averageProgress: Float?,
): String {
    val countPart = when (bookCount) {
        1 -> "One book on the go"
        else -> "$bookCount books on the go"
    }

    val progressPart = averageProgress?.let { "${it.roundToInt()}% through, on average" }

    return listOfNotNull(countPart, progressPart).joinToString(" · ")
}
