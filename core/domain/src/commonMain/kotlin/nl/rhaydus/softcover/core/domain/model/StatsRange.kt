package nl.rhaydus.softcover.core.domain.model

import kotlinx.datetime.LocalDate

/** The dates a stats aggregation covers. */
sealed interface StatsRange {
    /** Both ends are inclusive. */
    data class Bounded(
        val start: LocalDate,
        val endInclusive: LocalDate,
    ) : StatsRange {
        operator fun contains(date: LocalDate): Boolean = date >= start && date <= endInclusive
    }

    /** Covers every book, including those with no finish date. */
    data object Unbounded : StatsRange
}
