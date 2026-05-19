package nl.rhaydus.softcover.feature.personal.domain.model

import java.time.Instant
import java.time.LocalDate

data class ReadingLogEntry(
    val id: Long,
    val bookId: Int,
    val startedAt: LocalDate?,
    val finishedAt: LocalDate?,
    val rating: Double?,
    val note: String?,
    val createdAt: Instant,
)
