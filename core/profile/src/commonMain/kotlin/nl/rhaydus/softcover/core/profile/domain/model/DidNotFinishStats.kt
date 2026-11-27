package nl.rhaydus.softcover.core.profile.domain.model

/**
 * The Did not finish books of a stats range. [quarterCounts] holds the books whose stop could be placed,
 * every quarter present; the rest are in [unknownStopCount], so the two sum to [bookCount].
 */
data class DidNotFinishStats(
    val bookCount: Int = 0,
    val quarterCounts: Map<StopQuarter, Int> = StopQuarter.entries.associateWith { 0 },
    val unknownStopCount: Int = 0,
    val genres: GenreBreakdown = GenreBreakdown(),
)
