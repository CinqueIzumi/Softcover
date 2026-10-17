package nl.rhaydus.softcover.core.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The wall time and the open pause are each floored to whole seconds before they are subtracted.
 */
fun readingDuration(
    startedAtEpochMs: Long,
    pausedSeconds: Int,
    lastPausedAtEpochMs: Long?,
    endEpochMs: Long,
): Duration {
    val openPause = lastPausedAtEpochMs?.let { (endEpochMs - it) / MILLIS_PER_SECOND } ?: 0L
    val wall = (endEpochMs - startedAtEpochMs) / MILLIS_PER_SECOND

    return (wall - pausedSeconds - openPause).coerceAtLeast(0L).seconds
}

private const val MILLIS_PER_SECOND = 1_000L
