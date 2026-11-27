package nl.rhaydus.softcover.core.profile.domain.mapper

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.StopPoint
import kotlin.math.roundToInt

private const val PERCENT = 100.0

/**
 * Where this book was stopped, taken from its newest read alone: when that read is finished the stop
 * is unknown. The page falls back to the read's progress percentage of its edition's page count, then
 * of [statsPages]; the fraction prefers the progress percentage over the page.
 */
internal fun Book.toStopPoint(): StopPoint {
    val read = userBook?.readHistory?.firstOrNull()

    if (read == null || read.isFinished) {
        return StopPoint(
            page = null,
            fraction = null,
        )
    }

    val pages = (read.editionPages ?: statsPages()).takeIf { it > 0 }
    val progress = read.progress.toDouble().takeIf { it > 0.0 }

    val currentPage = read.currentPage

    val page = currentPage ?: if (progress != null && pages != null) {
        (progress / PERCENT * pages).roundToInt()
    } else {
        null
    }

    val fraction = when {
        progress != null -> progress / PERCENT
        currentPage != null && pages != null -> currentPage.toDouble() / pages
        else -> null
    }

    return StopPoint(
        page = page,
        fraction = fraction?.coerceIn(
            0.0,
            1.0,
        ),
    )
}
