package nl.rhaydus.softcover.core.personal.domain.model

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import kotlin.math.ceil

/**
 * A pace-based finish estimate for a book currently being read: the average units logged per
 * reading day, projected forward over the remaining units.
 */
data class ReadingPaceForecast(
    val avgPerReadingDay: Float,
    val remainingUnits: Int,
    val forecastReadingDays: Int,
    val unit: DeadlineUnit,
) {
    companion object {
        /**
         * Forecasts from a book's progress-journal [history], scoped to the current read-through
         * when [since] is given: entries dated before it are dropped before diffing, so the first
         * in-range day is measured from position 0.
         *
         * Accepted limits: entries from a read finished on the day the new one started leak in
         * (date granularity), and a read with no start date ([since] null) is unfiltered.
         *
         * Journal entries carry a cumulative position, so the last position logged on each
         * calendar date is diffed against the previous date's, clamped to 0 so a correction never
         * reads as negative progress.
         */
        fun compute(
            history: List<ReadingJournalEntry>,
            unit: DeadlineUnit,
            current: Int,
            total: Int,
            since: LocalDate?,
        ): ReadingPaceForecast? = compute(
            dailyUnitsRead = dailyUnitsRead(
                entries = history.filter { since == null || it.date >= since },
                unit = unit,
            ),
            current = current,
            total = total,
            unit = unit,
        )

        /**
         * @param dailyUnitsRead units logged per calendar day, one entry per day in range,
         * zero for a day with no logged reading. Days with a zero entry are excluded from the
         * average (e.g. `[50, 0, 30]` averages 40/day over the two active days).
         */
        fun compute(
            dailyUnitsRead: List<Int>,
            current: Int,
            total: Int,
            unit: DeadlineUnit,
        ): ReadingPaceForecast? {
            if (total <= 0) return null

            val remainingUnits = total - current
            if (remainingUnits <= 0) return null

            val readingDayTotals = dailyUnitsRead.filter { it > 0 }
            if (readingDayTotals.isEmpty()) return null

            val avgPerReadingDay = readingDayTotals.average().toFloat()
            if (avgPerReadingDay <= 0f) return null

            val forecastReadingDays = ceil(remainingUnits / avgPerReadingDay).toInt()

            return ReadingPaceForecast(
                avgPerReadingDay = avgPerReadingDay,
                remainingUnits = remainingUnits,
                forecastReadingDays = forecastReadingDays,
                unit = unit,
            )
        }

        private fun dailyUnitsRead(
            entries: List<ReadingJournalEntry>,
            unit: DeadlineUnit,
        ): List<Int> {
            val positionsByDate = entries
                .mapNotNull { entry ->
                    val position = when (unit) {
                        DeadlineUnit.PAGES -> entry.pages
                        DeadlineUnit.SECONDS -> entry.seconds
                    }

                    position?.let { entry.date to it }
                }
                .groupBy(
                    keySelector = { it.first },
                    valueTransform = { it.second },
                )
                .mapValues { (_, positions) -> positions.last() }
                .toList()
                .sortedBy { (date, _) -> date }
                .map { (_, position) -> position }

            var previousPosition = 0

            return positionsByDate.map { position ->
                val delta = (position - previousPosition).coerceAtLeast(0)
                previousPosition = position
                delta
            }
        }
    }
}
