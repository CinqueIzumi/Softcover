package nl.rhaydus.softcover.feature.widget.currentlyreading

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import kotlin.math.roundToInt

internal val WIDGET_WIDTHS_DP = listOf(110, 160, 210, 260, 320, 400)

/** The bottom row's content width: the widget minus the column padding and the row padding on both sides. */
internal fun progressBarWidthDp(widgetWidth: Dp): Int =
    (widgetWidth - (Spacing.xxs + Spacing.xs) * 2).value.roundToInt()

internal fun progressBarWidthsDp(): List<Int> = WIDGET_WIDTHS_DP.map { progressBarWidthDp(it.dp) }
