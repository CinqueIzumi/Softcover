package nl.rhaydus.softcover.core.component.chart

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Index of the column under [x] in a row of [count] equal-width columns separated by [gap], or -1 when
 * there are no columns or the row has no width. A gap belongs to the nearer of its two neighbours, and an
 * [x] outside the row snaps to the first or last column.
 */
internal fun columnIndexAt(
    x: Float,
    rowWidth: Float,
    count: Int,
    gap: Float,
): Int {
    if (count <= 0 || rowWidth <= 0f) return -1

    val pitch = (rowWidth - gap * (count - 1)) / count + gap

    return floor((x + gap / 2f) / pitch).toInt().coerceIn(
        0,
        count - 1,
    )
}

internal fun columnCenterX(
    index: Int,
    rowWidth: Float,
    count: Int,
    gap: Float,
): Float {
    val columnWidth = (rowWidth - gap * (count - 1)) / count

    return index * (columnWidth + gap) + columnWidth / 2f
}

/**
 * Valid only inside the [index]th of [count] equal-width columns separated by [gap]: the incoming max width
 * is taken as the column's width to work out where the row's edges are.
 */
internal fun Modifier.keepInsideRow(
    index: Int,
    count: Int,
    gap: Float,
): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
        ),
    )
    val columnWidth = constraints.maxWidth
    val columnLeft = index * (columnWidth + gap)
    val rowWidth = count * columnWidth + (count - 1) * gap
    val centredLeft = columnLeft + (columnWidth - placeable.width) / 2f
    val left = centredLeft.coerceIn(
        0f,
        (rowWidth - placeable.width).coerceAtLeast(0f),
    )

    layout(
        columnWidth,
        placeable.height,
    ) {
        placeable.place(
            x = (left - columnLeft).roundToInt(),
            y = 0,
        )
    }
}
