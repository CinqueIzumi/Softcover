package nl.rhaydus.softcover.feature.widget.presentation.util

import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

/**
 * The height the cover box gets in the Random pick layout shown at [widgetHeightDp]: what the eyebrow, title lines,
 * author and shuffle button leave of the widget. The compact layout drops the text, so it leaves the most per height.
 */
internal fun randomPickCoverHeightDp(widgetHeightDp: Int): Int {
    val padding = Spacing.xxs.value.toInt() * 2

    val chrome = if (widgetHeightDp >= TALL_HEIGHT_DP) {
        val titleLines = if (widgetHeightDp >= TALLEST_HEIGHT_DP) 2 else 1
        val shuffle = if (widgetHeightDp >= TALLEST_HEIGHT_DP) 48 else 36

        padding + EYEBROW_DP + Spacing.xxs.value.toInt() + titleLines * TITLE_LINE_DP + AUTHOR_DP + shuffle
    } else {
        padding + 36
    }

    return (widgetHeightDp - chrome).coerceAtLeast(0)
}

/**
 * The tallest cover box any size the launcher can give the widget shows. A layout stays in use until the next
 * declared height, and the last one keeps being used up to [TALLEST_WIDGET_HEIGHT_DP].
 */
internal fun maxRandomPickCoverHeightDp(): Int = (PORTRAIT_HEIGHTS_DP.drop(1) + TALLEST_WIDGET_HEIGHT_DP)
    .maxOf { upperBound ->
        randomPickCoverHeightDp(
            widgetHeightDp = upperBound - 1,
        )
    }

private const val TALLEST_WIDGET_HEIGHT_DP = 320
private const val EYEBROW_DP = 28
private const val TITLE_LINE_DP = 20
private const val AUTHOR_DP = 16
