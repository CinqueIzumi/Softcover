package nl.rhaydus.softcover.feature.widget.presentation.util

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

internal const val PROGRESS_BAR_HEIGHT_DP = 12

internal const val PROGRESS_ROW_HEIGHT_DP = 40

/** Room for "100%", so the bar and the edit glyph keep their place as the digits change. */
internal const val PROGRESS_LABEL_WIDTH_DP = 34

private const val PROGRESS_FIXED_RESERVE_DP = 30

/** The foot row's bar width: the room the layout gives the row, minus the percent label and the edit glyph. */
internal fun progressBarWidthDp(size: DpSize): Int {
    val rowWidth = if (isSideBySide(size)) {
        sideColumnWidthDp(size)
    } else {
        size.width.value.toInt() - columnPaddingDp()
    }

    return (rowWidth.dp - PROGRESS_LABEL_WIDTH_DP.dp - PROGRESS_FIXED_RESERVE_DP.dp)
        .value
        .roundToInt()
        .coerceAtLeast(PROGRESS_BAR_HEIGHT_DP)
}

internal fun progressBarWidthsDp(sizes: List<DpSize>): Set<Int> = sizes
    .map { progressBarWidthDp(it) }
    .toSet()
