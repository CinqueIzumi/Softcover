package nl.rhaydus.softcover.feature.session.presentation

import java.time.Duration

/** Formats a reading-session duration as `H:MM:SS` (or `M:SS` under an hour) for the live timer. */
fun formatSessionElapsed(elapsed: Duration): String {
    val totalSeconds = elapsed.seconds.coerceAtLeast(0L)
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}
