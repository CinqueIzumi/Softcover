package nl.rhaydus.softcover.feature.profile.presentation.mapper

import nl.rhaydus.softcover.core.component.chart.YearGridStep
import kotlin.math.ceil

private const val MIN_DAYS_FOR_QUARTILES = 4
private const val FIRST_QUARTILE = 0.25
private const val MEDIAN = 0.5
private const val THIRD_QUARTILE = 0.75

internal data class QuartileCutPoints(
    val first: Int,
    val second: Int,
    val third: Int,
)

/** Null when there are fewer than four values or they are all equal, so no step can be told apart. */
internal fun quartileCutPoints(values: Collection<Int>): QuartileCutPoints? {
    if (values.size < MIN_DAYS_FOR_QUARTILES || values.toSet().size == 1) return null

    val sorted = values.sorted()

    return QuartileCutPoints(
        first = nearestRank(
            sorted = sorted,
            fraction = FIRST_QUARTILE,
        ),
        second = nearestRank(
            sorted = sorted,
            fraction = MEDIAN,
        ),
        third = nearestRank(
            sorted = sorted,
            fraction = THIRD_QUARTILE,
        ),
    )
}

/** Value cut points, so equal counts share a step; without [cutPoints] every read day is the darkest step. */
internal fun stepFor(
    pages: Int,
    cutPoints: QuartileCutPoints?,
): YearGridStep = when {
    cutPoints == null -> YearGridStep.Four
    pages <= cutPoints.first -> YearGridStep.One
    pages <= cutPoints.second -> YearGridStep.Two
    pages <= cutPoints.third -> YearGridStep.Three
    else -> YearGridStep.Four
}

private fun nearestRank(
    sorted: List<Int>,
    fraction: Double,
): Int {
    val rank = ceil(fraction * sorted.size).toInt()
    val index = (rank - 1).coerceIn(
        minimumValue = 0,
        maximumValue = sorted.lastIndex,
    )

    return sorted[index]
}
