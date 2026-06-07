package nl.rhaydus.softcover.core.personal.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class ReadingLogEntry(
    val id: Long,
    val bookId: Int,
    val startedAt: LocalDate?,
    val finishedAt: LocalDate?,
    val rating: Double?,
    val note: String?,
    val createdAt: Instant,
)
