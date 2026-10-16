package nl.rhaydus.softcover.feature.widget.presentation.util

/** Height at or above which a cell stacks its text under the cover instead of beside it. */
internal const val STACKED_MIN_HEIGHT_DP = 150

/** The cover height that fills what a widget of [widgetHeightDp] leaves after its header, text and page row. */
internal fun trendingCoverHeightDp(
    widgetHeightDp: Int,
    showsPageRow: Boolean,
): Int {
    val stacked = widgetHeightDp >= STACKED_MIN_HEIGHT_DP

    val reserved = PADDING_DP + HEADER_DP +
        (if (showsPageRow) PAGE_ROW_DP else 0) +
        (if (stacked) TEXT_BLOCK_DP else 0)

    return (widgetHeightDp - reserved).coerceAtLeast(0)
}

private const val PADDING_DP = 8
private const val HEADER_DP = 28
private const val PAGE_ROW_DP = 28
private const val TEXT_BLOCK_DP = 42
