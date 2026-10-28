package nl.rhaydus.softcover.core.profile.domain.model

/**
 * Median calendar days from start to finish.
 *
 * @property medianDays null when [readCount] is 0.
 * @property bandMedianDays has no key for a band no counted read falls in.
 * @property excludedReadCount reads finished in range that could not count: no start date, a finish
 * date that is not day-precise, or a finish before the start.
 */
data class TimeToFinish(
    val readCount: Int,
    val medianDays: Int?,
    val bandMedianDays: Map<LengthBand, Int>,
    val excludedReadCount: Int,
)
