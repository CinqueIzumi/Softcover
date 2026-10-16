package nl.rhaydus.softcover.feature.widget.presentation.util

/** Height at or above which a cell stacks its text under the cover instead of beside it. */
internal const val STACKED_MIN_HEIGHT_DP = 150

/**
 * The cover height that fills what a widget of [widgetHeightDp] leaves after its header, text and page row,
 * capped so a 2:3 cover fits the cell width a widget of [widgetWidthDp] gives each of its two books.
 */
internal fun trendingCoverHeightDp(
    widgetHeightDp: Int,
    widgetWidthDp: Int,
    showsPageRow: Boolean,
): Int {
    val stacked = widgetHeightDp >= STACKED_MIN_HEIGHT_DP

    val reserved = PADDING_DP + HEADER_DP +
        (if (showsPageRow) PAGE_ROW_DP else 0) +
        (if (stacked) TEXT_BLOCK_DP else 0)

    val fitsHeight = (widgetHeightDp - reserved).coerceAtLeast(0)
    val cellWidth = (widgetWidthDp - PADDING_DP) / 2 - CELL_PADDING_DP
    val fitsWidth = (cellWidth * 3 / 2).coerceAtLeast(0)

    return minOf(
        fitsHeight,
        fitsWidth,
    )
}

private const val PADDING_DP = 8
private const val HEADER_DP = 28
private const val PAGE_ROW_DP = 28
private const val TEXT_BLOCK_DP = 42
private const val CELL_PADDING_DP = 8
