package nl.rhaydus.softcover.feature.profile.presentation.mapper

private const val PERCENT = 100

/**
 * Whole percents by largest remainder so they sum to 100. A non-zero count never shows 0%: it shows 1% and
 * the largest share gives up the point.
 */
internal fun wholePercents(
    counts: List<Int>,
    total: Int,
): List<Int> {
    if (total <= 0 || counts.isEmpty()) return counts.map { 0 }

    val floors = counts.map { it * PERCENT / total }.toMutableList()
    val remainders = counts.map { it * PERCENT % total }
    val missing = PERCENT - floors.sum()

    remainders.indices
        .sortedWith(compareByDescending<Int> { remainders[it] }.thenBy { it })
        .take(missing)
        .forEach { floors[it] += 1 }

    counts.indices.filter { counts[it] > 0 && floors[it] == 0 }.forEach { index ->
        val largest = floors.indices.maxByOrNull { floors[it] } ?: return@forEach

        floors[largest] -= 1
        floors[index] = 1
    }

    return floors
}
