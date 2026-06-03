package nl.rhaydus.softcover.core.designsystem.presentation.util

fun secondsToHm(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    return "${hours}h ${minutes}m"
}

internal fun secondsToClock(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val secs = safe % 60
    return "%02d:%02d:%02d".format(
        hours,
        minutes,
        secs,
    )
}

internal fun Int.toHoursMinutesSeconds(): HoursMinutesSeconds {
    val safe = coerceAtLeast(0)
    return HoursMinutesSeconds(
        hours = safe / 3600,
        minutes = (safe % 3600) / 60,
        seconds = safe % 60,
    )
}
