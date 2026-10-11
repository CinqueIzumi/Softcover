package nl.rhaydus.softcover.core.component.chart

import kotlin.math.floor

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
