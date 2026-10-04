package nl.rhaydus.softcover.feature.reading.presentation.mapper

import nl.rhaydus.softcover.core.personal.domain.model.ReadingPaceForecast
import nl.rhaydus.softcover.core.uibinding.deadline.formatPacePerDay
import kotlin.math.roundToInt

/** "35 pages/day — about 5 days to go" (or the audiobook time equivalent). */
internal fun ReadingPaceForecast.toPaceLine(): String {
    val pace = formatPacePerDay(
        amount = avgPerReadingDay.roundToInt(),
        unit = unit,
    )

    return "$pace — about $forecastReadingDays ${if (forecastReadingDays == 1) "day" else "days"} to go"
}
