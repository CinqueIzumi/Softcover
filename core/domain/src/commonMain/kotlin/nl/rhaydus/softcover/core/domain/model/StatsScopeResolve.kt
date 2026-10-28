package nl.rhaydus.softcover.core.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

private const val LAST_30_DAYS_SPAN = 30
private const val HALF_YEAR_MONTHS = 6
private const val YEAR_MONTHS = 12

/**
 * Rolling windows end on [today] and include it. Month windows start the day after the same date
 * [HALF_YEAR_MONTHS] or [YEAR_MONTHS] months back, which clamps to the month's last day when that
 * date does not exist.
 */
fun StatsScope.resolve(today: LocalDate): StatsRange = when (this) {
    is StatsScope.Rolling -> StatsRange.Bounded(
        start = when (window) {
            StatsWindow.LAST_30_DAYS -> today.minus(
                LAST_30_DAYS_SPAN - 1,
                DateTimeUnit.DAY,
            )
            StatsWindow.LAST_6_MONTHS -> today.minus(
                HALF_YEAR_MONTHS,
                DateTimeUnit.MONTH,
            ).plus(
                1,
                DateTimeUnit.DAY,
            )
            StatsWindow.LAST_12_MONTHS -> today.minus(
                YEAR_MONTHS,
                DateTimeUnit.MONTH,
            ).plus(
                1,
                DateTimeUnit.DAY,
            )
        },
        endInclusive = today,
    )

    is StatsScope.CalendarYear -> StatsRange.Bounded(
        start = LocalDate(
            year,
            1,
            1,
        ),
        endInclusive = LocalDate(
            year,
            12,
            31,
        ),
    )

    StatsScope.AllTime -> StatsRange.Unbounded
}
