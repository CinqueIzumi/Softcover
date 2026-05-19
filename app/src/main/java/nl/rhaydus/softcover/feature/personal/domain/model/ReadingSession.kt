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
) {
    val isActive: Boolean get() = endedAt == null

    val duration: Duration?
        get() = endedAt?.let { Duration.between(startedAt, it) }

    val pageDelta: Int?
        get() = if (startPage != null && endPage != null) endPage - startPage else null

    val secondsDelta: Int?
        get() = if (startSeconds != null && endSeconds != null) endSeconds - startSeconds else null
}
