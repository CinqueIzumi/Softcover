package nl.rhaydus.softcover.feature.reading.presentation.screen

import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.softcover.core.domain.model.Book
import kotlin.math.roundToInt

internal fun List<Book>.averageProgress(): Float? {
    if (isEmpty()) return null
    val values = mapNotNull { it.userBookRead?.progress }
    if (values.isEmpty()) return null
    return values.average().toFloat()
}

internal fun greetingForNow(): String {
    val hour = currentLocalDateTime().hour
    return when (hour) {
        in 5..11 -> "Good morning."
        in 12..17 -> "Good afternoon."
        in 18..21 -> "Good evening."
        else -> "Late hours."
    }
}

internal fun buildSubtitle(
    bookCount: Int,
    averageProgress: Float?,
): String {
    val countPart = when (bookCount) {
        1 -> "One title in motion"
        else -> "$bookCount titles in motion"
    }

    val progressPart = averageProgress?.let { "${it.roundToInt()}% along, on average" }

    return listOfNotNull(countPart, progressPart).joinToString(" · ")
}
