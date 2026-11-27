package nl.rhaydus.softcover.core.progresssheet.presentation.mood

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressEntry
import nl.rhaydus.softcover.core.uibinding.progress.resolvedTotalPages
import kotlin.math.roundToInt

private const val PERCENT_MAX = 100

data class PendingMoodMoment(
    val bookId: Int,
    val page: Int?,
    val seconds: Int?,
    val percent: Int?,
)

fun ProgressEntry.toPendingMoodMoment(book: Book): PendingMoodMoment {
    val total = if (newSeconds != null) {
        book.currentEdition?.audioSeconds ?: 0
    } else {
        book.resolvedTotalPages()
    }

    val position = newSeconds ?: newPage ?: 0

    return PendingMoodMoment(
        bookId = book.id,
        page = newPage,
        seconds = newSeconds,
        percent = if (total > 0) {
            (position * PERCENT_MAX.toDouble() / total).roundToInt().coerceIn(
                0,
                PERCENT_MAX,
            )
        } else {
            null
        },
    )
}
