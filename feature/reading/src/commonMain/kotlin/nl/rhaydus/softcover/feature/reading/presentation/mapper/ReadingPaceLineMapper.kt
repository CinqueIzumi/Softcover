package nl.rhaydus.softcover.feature.reading.presentation.mapper

import nl.rhaydus.softcover.core.personal.domain.model.ReadingPaceForecast
import nl.rhaydus.softcover.core.uibinding.deadline.formatPacePerDay
import kotlin.math.roundToInt

internal fun ReadingPaceForecast.toPaceLine(): String {
    val pace = formatPacePerDay(
        amount = avgPerReadingDay.roundToInt(),
        unit = unit,
    )

    return "$pace · about $forecastReadingDays ${if (forecastReadingDays == 1) "day" else "days"} to go"
}
