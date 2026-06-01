package nl.rhaydus.softcover.feature.personal.domain.model

import java.time.Duration
import java.time.Instant

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
        get() = endedAt?.let { Duration.between(startedAt, it) }

    val pageDelta: Int?
        get() = if (startPage != null && endPage != null) endPage - startPage else null

    val secondsDelta: Int?
        get() = if (startSeconds != null && endSeconds != null) endSeconds - startSeconds else null

    /**
     * Wall-clock time since [startedAt] minus all paused time, folding any still-open pause up to
     * [now]. This is the honest reading time and the single source of truth for the live timer
     * (peek bar, Focus Mode, and the lock-screen chronometer all derive from it).
     */
    fun readingDuration(now: Instant = Instant.now()): Duration {
        val end = endedAt ?: now
        val openPause = lastPausedAt?.let { Duration.between(it, end).seconds } ?: 0L
        val wall = Duration.between(startedAt, end).seconds

        return Duration.ofSeconds((wall - pausedSeconds - openPause).coerceAtLeast(0L))
    }
}
