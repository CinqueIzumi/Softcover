package nl.rhaydus.softcover.core.component.bookcard

/**
 * How many leading chips fit on one line of [available] pixels, spaced by [gap], once the overflow chip for
 * the hidden remainder is counted. [overflowWidths] is indexed by hidden count minus one, as
 * [BookCardTagStrip.overflowChips] is.
 */
internal fun fittingChipCount(
    chipWidths: List<Int>,
    overflowWidths: List<Int>,
    gap: Int,
    available: Int,
): Int {
    val total = chipWidths.size

    if (chipWidths.sum() + gap * (total - 1).coerceAtLeast(0) <= available) return total

    return (total - 1 downTo 1).firstOrNull { shown ->
        val shownWidth = chipWidths.take(shown).sum() + gap * shown
        val overflowWidth = overflowWidths.getOrNull(total - shown - 1) ?: 0

        shownWidth + overflowWidth <= available
    } ?: 0
}
