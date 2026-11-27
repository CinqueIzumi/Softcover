package nl.rhaydus.softcover.core.profile.domain.model

private const val SECOND_QUARTER_START = 0.25
private const val THIRD_QUARTER_START = 0.5
private const val LAST_QUARTER_START = 0.75

/**
 * Where a reader stopped a Did not finish book; each part is null when the book does not say.
 * [fraction] is the share of the book reached, 0 to 1.
 */
data class StopPoint(
    val page: Int?,
    val fraction: Double?,
) {
    val quarter: StopQuarter?
        get() = fraction?.let {
            when {
                it < SECOND_QUARTER_START -> StopQuarter.FIRST
                it < THIRD_QUARTER_START -> StopQuarter.SECOND
                it < LAST_QUARTER_START -> StopQuarter.THIRD
                else -> StopQuarter.LAST
            }
        }
}
