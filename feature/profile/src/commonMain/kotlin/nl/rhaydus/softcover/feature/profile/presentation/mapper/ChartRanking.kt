package nl.rhaydus.softcover.feature.profile.presentation.mapper

import nl.rhaydus.softcover.core.component.chart.ChartTone

private const val SECOND_TONE_COUNT = 2

/**
 * The tone of each column by rank: the biggest [ChartTone.Lead], the next two [ChartTone.Second], the
 * other non-zero ones [ChartTone.Third] and empty ones [ChartTone.Fifth]. Ties go to the earliest column,
 * so the order is stable between scopes.
 */
internal fun rankedColumnTones(values: List<Int>): List<ChartTone> {
    val ranked = rankedIndices(values)

    return values.indices.map { index ->
        when (ranked.indexOf(index)) {
            -1 -> ChartTone.Fifth
            0 -> ChartTone.Lead
            in 1..SECOND_TONE_COUNT -> ChartTone.Second
            else -> ChartTone.Third
        }
    }
}

/** The index of the biggest value, the earliest on a tie; null when every value is zero. */
internal fun peakIndex(values: List<Int>): Int? = rankedIndices(values).firstOrNull()

private fun rankedIndices(values: List<Int>): List<Int> = values
    .withIndex()
    .filter { it.value > 0 }
    .sortedWith(compareByDescending<IndexedValue<Int>> { it.value }.thenBy { it.index })
    .map { it.index }
