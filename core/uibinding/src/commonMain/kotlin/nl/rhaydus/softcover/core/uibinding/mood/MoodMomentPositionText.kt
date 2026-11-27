package nl.rhaydus.softcover.core.uibinding.mood

import nl.rhaydus.common.toHoursMinutesSeconds

/** "Page 286 · 62%", "1:02:03 · 62%" for an audiobook, and "62%" alone when the book has no total. */
fun moodMomentEyebrow(
    page: Int?,
    seconds: Int?,
    percent: Int?,
): String = listOfNotNull(
    seconds?.toClock() ?: page?.let { "Page $it" },
    percent?.let { "$it%" },
).joinToString(" · ")

/** A moment row's position: the percent, or the raw page or time when the book has no total. */
fun moodMomentAt(
    page: Int?,
    seconds: Int?,
    percent: Int?,
): String = percent?.let { "$it%" } ?: page?.let { "p. $it" } ?: seconds?.toClock().orEmpty()

private fun Int.toClock(): String {
    val parts = toHoursMinutesSeconds()

    return "${parts.hours}:${parts.minutes.toString().padStart(
        2,
        '0',
    )}:${parts.seconds.toString().padStart(
        2,
        '0',
    )}"
}
