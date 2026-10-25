package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.model.HourlyPages

private const val HOURS_IN_DAY = 24

/**
 * Sums the pages read per local hour in [timeZone] from the [updates] logged on a date in [range]. A
 * finish date carries no time of day, so the unlogged remainder of a finished book is left out.
 */
internal fun toHourlyPages(
    updates: List<ProgressUpdate>,
    range: StatsRange,
    timeZone: TimeZone,
): HourlyPages {
    val pagesPerHour = IntArray(HOURS_IN_DAY)

    updates.forEach { update ->
        val local = update.at.toLocalDateTime(timeZone)

        if (update.pagesRead > 0 && local.date in range) {
            pagesPerHour[local.hour] += update.pagesRead
        }
    }

    return HourlyPages(pages = pagesPerHour.toList())
}
