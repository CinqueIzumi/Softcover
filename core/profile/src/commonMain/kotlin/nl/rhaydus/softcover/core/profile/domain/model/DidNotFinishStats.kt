package nl.rhaydus.softcover.core.profile.domain.model

import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason

/**
 * The Did not finish books of a stats range. [quarterCounts] holds the books whose stop could be placed,
 * every quarter present; the rest are in [unknownStopCount], so the two sum to [bookCount].
 * [reasonCounts] counts the books naming each reason, every reason present, so it can exceed [bookCount];
 * [otherReasonCount] counts the books with only their own words.
 */
data class DidNotFinishStats(
    val bookCount: Int = 0,
    val quarterCounts: Map<StopQuarter, Int> = StopQuarter.entries.associateWith { 0 },
    val unknownStopCount: Int = 0,
    val reasonCounts: Map<DidNotFinishReason, Int> = DidNotFinishReason.entries.associateWith { 0 },
    val otherReasonCount: Int = 0,
    val genres: GenreBreakdown = GenreBreakdown(),
)
