package nl.rhaydus.softcover.core.component.cover

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import kotlin.math.roundToInt

/**
 * The smallest font size the monogram ever renders at; below it a glyph stops reading as a
 * letterform.
 */
internal val MONOGRAM_MIN_LEGIBLE_FONT_SIZE = 10.sp

private const val BORDER_INSET_FRACTION = 0.06f

private const val INITIAL_FONT_SIZE_FRACTION = 0.36f
private val INITIAL_FONT_SIZE_CEIL = 96.sp

private const val TITLE_FONT_SIZE_FRACTION = 0.15f
private val TITLE_FONT_SIZE_CEIL = 28.sp

private const val TITLE_MAX_LINES_DIVISOR = 32f
private const val TITLE_MAX_LINES_FLOOR = 2
private const val TITLE_MAX_LINES_CEIL = 5

internal data class MonogramCoverMetrics(
    val borderInset: Dp,
    val initialFontSize: TextUnit,
    val showFullTitle: Boolean,
    val titleFontSize: TextUnit,
    val titleMaxLines: Int,
)

/**
 * [CoverlessTitleCover]'s fit for a tile of [width]. [MonogramCoverMetrics.titleFontSize] is only a
 * starting size: the caller still shrinks it per title via `TextAutoSize`, because the fit also
 * depends on the title's length.
 */
internal fun monogramCoverMetrics(width: Dp): MonogramCoverMetrics {
    val borderInset = (width * BORDER_INSET_FRACTION).coerceIn(
        Spacing.xxxs,
        Spacing.xs,
    )

    val initialFontSizeSp = (width.value * INITIAL_FONT_SIZE_FRACTION)
        .coerceIn(
            MONOGRAM_MIN_LEGIBLE_FONT_SIZE.value,
            INITIAL_FONT_SIZE_CEIL.value,
        )

    val titleFontSizeSp = (width.value * TITLE_FONT_SIZE_FRACTION)
        .coerceIn(
            MONOGRAM_MIN_LEGIBLE_FONT_SIZE.value,
            TITLE_FONT_SIZE_CEIL.value,
        )

    val titleMaxLines = (width.value / TITLE_MAX_LINES_DIVISOR)
        .roundToInt()
        .coerceIn(
            TITLE_MAX_LINES_FLOOR,
            TITLE_MAX_LINES_CEIL,
        )

    return MonogramCoverMetrics(
        borderInset = borderInset,
        initialFontSize = initialFontSizeSp.sp,
        showFullTitle = width >= 64.dp,
        titleFontSize = titleFontSizeSp.sp,
        titleMaxLines = titleMaxLines,
    )
}
