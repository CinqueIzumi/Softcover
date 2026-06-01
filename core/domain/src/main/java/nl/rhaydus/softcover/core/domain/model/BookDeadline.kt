package nl.rhaydus.softcover.core.domain.model

import java.time.LocalDate

data class BookDeadline(
    val bookId: Int,
    val deadlineDate: LocalDate,
    val setAt: LocalDate,
    val initialPerDay: Float,
    val unit: DeadlineUnit,
)
