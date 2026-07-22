package nl.rhaydus.softcover.core.personal.domain.model

import kotlin.math.ceil
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit

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
    }
}
