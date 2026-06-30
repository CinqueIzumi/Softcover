package nl.rhaydus.softcover.core.designsystem.presentation.session

import kotlin.time.Duration

/** Formats a reading-session duration as `H:MM:SS` (or `M:SS` under an hour) for the live timer. */
fun formatSessionElapsed(elapsed: Duration): String {
    val totalSeconds = elapsed.inWholeSeconds.coerceAtLeast(0L)
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "$hours:${minutes.pad2()}:${seconds.pad2()}"
    } else {
        "$minutes:${seconds.pad2()}"
    }
}

private fun Long.pad2(): String = toString().padStart(
    2,
    '0',
)
