package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.model.PageCreditInputs
import nl.rhaydus.softcover.core.profile.domain.model.PagesRead
import kotlin.math.roundToInt

/**
 * Sums the [attributePages] credits dated in [range]. Under [StatsRange.Unbounded] every Read book
 * counts as finished and every Did not finish book with a stop date as stopped; the per-day average
 * then runs from the first credited day to [today].
 */
internal fun toPagesRead(
    inputs: PageCreditInputs,
    range: StatsRange,
    today: LocalDate,
    timeZone: TimeZone,
): PagesRead {
    val credits = attributePages(
        updates = inputs.updates,
        readBooks = inputs.readBooks,
        didNotFinishBooks = inputs.didNotFinishBooks,
        timeZone = timeZone,
    ).filter { it.date in range }

    val finishedIds = inputs.readBooks
        .filter { it.userBook?.finishDate.isIn(range) }
        .map { it.id }
        .toSet()

    val stoppedIds = inputs.didNotFinishBooks
        .filter { it.userBook?.stopDate.isIn(range) }
        .map { it.id }
        .toSet()

    val total = credits.sumOf { it.pages }

    return PagesRead(
        total = total,
        finishedBookCount = finishedIds.size,
        stoppedBookCount = stoppedIds.size,
        hasInProgressPages = credits.any { it.bookId !in finishedIds && it.bookId !in stoppedIds },
        pagesPerDay = pagesPerDay(
            total = total,
            range = range,
            firstCreditedDate = credits.minOfOrNull { it.date },
            today = today,
        ),
    )
}

// Under an unbounded range an undated book still counts; under a bounded one it cannot fall inside.
private fun LocalDate?.isIn(range: StatsRange): Boolean = when (range) {
    StatsRange.Unbounded -> true
    is StatsRange.Bounded -> this != null && this in range
}

private fun pagesPerDay(
    total: Int,
    range: StatsRange,
    firstCreditedDate: LocalDate?,
    today: LocalDate,
): Int? {
    val start = when (range) {
        StatsRange.Unbounded -> firstCreditedDate
        is StatsRange.Bounded -> range.start
    } ?: return null

    val end = when (range) {
        StatsRange.Unbounded -> today
        is StatsRange.Bounded -> minOf(
            range.endInclusive,
            today,
        )
    }

    val days = start.daysUntil(end) + 1

    if (total == 0 || days <= 0) return null

    return (total.toDouble() / days).roundToInt().takeIf { it >= 1 }
}
