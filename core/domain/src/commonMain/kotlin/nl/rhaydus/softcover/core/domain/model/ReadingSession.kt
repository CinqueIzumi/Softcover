package nl.rhaydus.softcover.core.domain.model

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

data class ReadingSession(
    val id: Long,
    val bookId: Int,
    val startedAt: Instant,
    val endedAt: Instant?,
    val startPage: Int?,
    val endPage: Int?,
    val startSeconds: Int?,
    val endSeconds: Int?,
    val pausedSeconds: Int = 0,
    val lastPausedAt: Instant? = null,
) {
    val isActive: Boolean get() = endedAt == null

    val isPaused: Boolean get() = isActive && lastPausedAt != null

    val duration: Duration?
        get() = endedAt?.let { it - startedAt }

    val pageDelta: Int?
        get() = if (startPage != null && endPage != null) endPage - startPage else null

    val secondsDelta: Int?
        get() = if (startSeconds != null && endSeconds != null) endSeconds - startSeconds else null

    fun readingDuration(now: Instant = Clock.System.now()): Duration =
        readingDuration(
            startedAtEpochMs = startedAt.toEpochMilliseconds(),
            pausedSeconds = pausedSeconds,
            lastPausedAtEpochMs = lastPausedAt?.toEpochMilliseconds(),
            endEpochMs = (endedAt ?: now).toEpochMilliseconds(),
        )
}
