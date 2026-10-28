package nl.rhaydus.softcover.core.profile.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** A run of consecutive reading days, both ends included. */
data class ReadingStreak(
    val start: LocalDate,
    val end: LocalDate,
) {
    val days: Int get() = start.daysUntil(end) + 1
}
