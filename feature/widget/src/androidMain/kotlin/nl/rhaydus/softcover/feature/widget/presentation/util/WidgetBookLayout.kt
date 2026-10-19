package nl.rhaydus.softcover.feature.widget.presentation.util

import androidx.compose.ui.unit.DpSize
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

internal const val TALL_HEIGHT_DP = 150
internal const val TALLEST_HEIGHT_DP = 180

internal fun columnPaddingDp(): Int = Spacing.xxs.value.toInt() * 2

internal fun isSideBySide(size: DpSize): Boolean = size.width > size.height

internal fun sideColumnHeightDp(size: DpSize): Int = (size.height.value.toInt() - columnPaddingDp())
    .coerceAtLeast(0)

internal fun sideCoverHeightDp(size: DpSize): Int = (sideColumnHeightDp(size) - Spacing.xs.value.toInt() * 2)
    .coerceAtLeast(0)

/** Glance cannot measure the image, so the side layout fixes the cover box to a 2:3 slot of its height. */
internal fun sideCoverWidthDp(size: DpSize): Int = sideCoverHeightDp(size) * 2 / 3

internal fun sideColumnWidthDp(size: DpSize): Int =
    (size.width.value.toInt() - columnPaddingDp() - sideCoverWidthDp(size) - Spacing.xs.value.toInt() * 2)
        .coerceAtLeast(0)

internal fun sideShowsAuthor(
    size: DpSize,
    fixedRowsDp: Int,
): Boolean = sideColumnHeightDp(size) - fixedRowsDp >= TITLE_LINE_DP + AUTHOR_DP

internal fun sideTitleLines(
    size: DpSize,
    fixedRowsDp: Int,
): Int {
    val showsAuthor = sideShowsAuthor(
        size = size,
        fixedRowsDp = fixedRowsDp,
    )

    val author = if (showsAuthor) AUTHOR_DP else 0

    return ((sideColumnHeightDp(size) - fixedRowsDp - author) / TITLE_LINE_DP).coerceIn(
        1,
        SIDE_TITLE_MAX_LINES,
    )
}

internal fun stackedTitleLines(heightDp: Int): Int = if (heightDp >= TALLEST_HEIGHT_DP) 2 else 1

internal fun stackedCoverHeightDp(
    heightDp: Int,
    eyebrowDp: Int,
    footDp: Int,
): Int {
    val text = if (heightDp >= TALL_HEIGHT_DP) {
        Spacing.xxs.value.toInt() + stackedTitleLines(heightDp) * TITLE_LINE_DP + AUTHOR_DP
    } else {
        0
    }

    return (heightDp - columnPaddingDp() - eyebrowDp - footDp - text).coerceAtLeast(0)
}

internal fun tallestCoverHeightDp(
    sizes: List<DpSize>,
    eyebrowDp: (heightDp: Int) -> Int,
    footDp: (heightDp: Int) -> Int,
): Int = sizes.maxOfOrNull { size ->
    if (isSideBySide(size)) {
        sideCoverHeightDp(size)
    } else {
        val heightDp = size.height.value.toInt()

        stackedCoverHeightDp(
            heightDp = heightDp,
            eyebrowDp = eyebrowDp(heightDp),
            footDp = footDp(heightDp),
        )
    }
}?.coerceAtLeast(MIN_COVER_HEIGHT_DP) ?: MIN_COVER_HEIGHT_DP

private const val MIN_COVER_HEIGHT_DP = 1
private const val SIDE_TITLE_MAX_LINES = 3
private const val TITLE_LINE_DP = 20
private const val AUTHOR_DP = 16
