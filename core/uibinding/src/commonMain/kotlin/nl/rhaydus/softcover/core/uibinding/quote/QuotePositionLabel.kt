package nl.rhaydus.softcover.core.uibinding.quote

import nl.rhaydus.softcover.core.domain.model.QuotePosition
import kotlin.math.roundToInt

private const val SECONDS_PER_HOUR = 3600
private const val SECONDS_PER_MINUTE = 60

/** "p. 214", "53%" or "1:02:03". */
fun QuotePosition.toPositionLabel(): String = when (this) {
    is QuotePosition.Pages -> "p. $page"
    is QuotePosition.Percent -> "${percent.roundToInt()}%"
    is QuotePosition.Seconds -> seconds.toQuoteClockText()
}

/** "1:02:03". */
fun Int.toQuoteClockText(): String {
    val hours = this / SECONDS_PER_HOUR
    val minutes = this % SECONDS_PER_HOUR / SECONDS_PER_MINUTE
    val seconds = this % SECONDS_PER_MINUTE

    return "$hours:${minutes.twoDigits()}:${seconds.twoDigits()}"
}

private fun Int.twoDigits(): String = toString().padStart(
    length = 2,
    padChar = '0',
)
