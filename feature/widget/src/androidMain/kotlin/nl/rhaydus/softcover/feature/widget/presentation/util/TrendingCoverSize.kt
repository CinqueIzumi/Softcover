package nl.rhaydus.softcover.feature.widget.presentation.util

import androidx.compose.ui.unit.DpSize
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import kotlin.math.ceil

/** Height at or above which a cell stacks its text under the cover instead of beside it. */
internal const val STACKED_MIN_HEIGHT_DP = 150

/** Caption text sizes, shared by the drawn caption and the [trendingCoverHeightDp] reserve under the cover. */
internal const val TRENDING_TITLE_SP = 12
internal const val TRENDING_AUTHOR_SP = 10
internal const val TRENDING_TITLE_MAX_LINES = 2

/**
 * The cover height that fills what a widget of [widgetHeightDp] leaves after its header, text and page row,
 * capped so a 2:3 cover fits the cell width a widget of [widgetWidthDp] gives each of its two books.
 * A stacked cell reserves its worst-case caption, which grows with [fontScale].
 */
internal fun trendingCoverHeightDp(
    widgetHeightDp: Int,
    widgetWidthDp: Int,
    showsPageRow: Boolean,
    fontScale: Float,
): Int {
    val stacked = widgetHeightDp >= STACKED_MIN_HEIGHT_DP

    val reserved = PADDING_DP + HEADER_DP +
        (if (showsPageRow) PAGE_ROW_DP else 0) +
        (if (stacked) captionReserveDp(fontScale) else 0)

    val fitsHeight = (widgetHeightDp - reserved).coerceAtLeast(0)
    val cellWidth = (widgetWidthDp - PADDING_DP) / 2 - CELL_PADDING_DP
    val fitsWidth = (cellWidth * 3 / 2).coerceAtLeast(0)

    return minOf(
        fitsHeight,
        fitsWidth,
    )
}

/** The tallest [trendingCoverHeightDp] over [sizes], without the page row so it never undershoots a drawn cover. */
internal fun trendingCoverHeightDp(
    sizes: List<DpSize>,
    fontScale: Float,
): Int = tallestOf(sizes) { size ->
    trendingCoverHeightDp(
        widgetHeightDp = size.height.value.toInt(),
        widgetWidthDp = size.width.value.toInt(),
        showsPageRow = false,
        fontScale = fontScale,
    )
}

private const val PADDING_DP = 8
private const val HEADER_DP = 28
private const val PAGE_ROW_DP = 28
private const val LINE_HEIGHT_FACTOR = 1.217f
private const val CELL_PADDING_DP = 8

private fun captionReserveDp(fontScale: Float): Int {
    val textSp = TRENDING_TITLE_SP * TRENDING_TITLE_MAX_LINES + TRENDING_AUTHOR_SP

    return Spacing.xxs.value.toInt() + ceil(textSp * LINE_HEIGHT_FACTOR * fontScale).toInt()
}
