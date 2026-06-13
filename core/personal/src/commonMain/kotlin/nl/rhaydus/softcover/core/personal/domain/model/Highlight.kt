package nl.rhaydus.softcover.core.personal.domain.model

import kotlin.time.Instant

data class Highlight(
    val id: Long,
    val bookId: Int,
    val quote: String,
    val page: Int?,
    val note: String?,
    val createdAt: Instant,
)
