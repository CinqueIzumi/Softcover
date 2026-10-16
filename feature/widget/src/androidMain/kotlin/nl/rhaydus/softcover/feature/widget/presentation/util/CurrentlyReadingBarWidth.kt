package nl.rhaydus.softcover.feature.widget.presentation.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import kotlin.math.roundToInt

internal val WIDGET_WIDTHS_DP = listOf(110, 180, 250, 320, 390)

internal const val COMPACT_HEIGHT_DP = 110
internal const val TALL_HEIGHT_DP = 150
internal const val TALLEST_HEIGHT_DP = 180

internal val PORTRAIT_HEIGHTS_DP = listOf(COMPACT_HEIGHT_DP, TALL_HEIGHT_DP, TALLEST_HEIGHT_DP)

internal const val PROGRESS_BAR_HEIGHT_DP = 12

private const val PROGRESS_LABEL_RESERVE_DP = 64

/** The foot row's bar width: the widget minus the column padding and the room the percent and edit glyph take. */
internal fun progressBarWidthDp(widgetWidth: Dp): Int =
    (widgetWidth - Spacing.xxs * 2 - PROGRESS_LABEL_RESERVE_DP.dp).value.roundToInt()

internal fun progressBarWidthsDp(): List<Int> = WIDGET_WIDTHS_DP.map { progressBarWidthDp(it.dp) }

internal fun portraitSizes(widthsDp: List<Int>): Set<DpSize> {
    val sizes = mutableSetOf<DpSize>()

    for (width in widthsDp) {
        for (height in PORTRAIT_HEIGHTS_DP) {
            sizes += DpSize(
                width = width.dp,
                height = height.dp,
            )
        }
    }

    return sizes
}
