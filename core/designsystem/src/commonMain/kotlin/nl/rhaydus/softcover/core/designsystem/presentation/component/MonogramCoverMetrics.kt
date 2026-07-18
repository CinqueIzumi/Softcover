package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * The smallest font size either the single-initial fallback or the full-title rendering ever
 * settles at — below this a glyph stops reading as a letterform. Shared so [CoverlessTitleCover]'s
 * `TextAutoSize` floor for the full title matches the same legibility line this file already draws
 * for the initial.
 */
internal val MONOGRAM_MIN_LEGIBLE_FONT_SIZE = 10.sp

private const val BORDER_INSET_FRACTION = 0.06f
private val BORDER_INSET_FLOOR = 3.dp
private val BORDER_INSET_CEIL = 8.dp

private const val INITIAL_FONT_SIZE_FRACTION = 0.36f
private val INITIAL_FONT_SIZE_CEIL = 96.sp

/**
 * Below this width, a full multi-word title can't wrap into anything legible — [CoverlessTitleCover]
 * degrades to the single-initial rendering instead. Set just above the 48dp shelf-thumbnail floor
 * documented on [CoverlessTitleCover], the narrowest real call site, which needs the initial.
 */
private val FULL_TITLE_MIN_WIDTH = 64.dp

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
 * Derives [CoverlessTitleCover]'s size-adaptive fit purely from the measured tile [width]: the
 * hairline border inset, the single-initial fallback's font size, and the full-title rendering's
 * starting font size / line cap all scale with the tile, floored so they never vanish on a
 * thumbnail-sized tile and capped so they never balloon on a very wide desktop tile. [showFullTitle]
 * is the degrade gate — below [FULL_TITLE_MIN_WIDTH] a wrapped title has no room to read, so
 * [CoverlessTitleCover] falls back to [initialFontSize] alone. Pure (non-composable) so the fit math
 * is independently testable; [titleFontSize] is a *starting* size only — the caller still shrinks it
 * further per-title via `TextAutoSize`, since the actual fit also depends on the title's own length,
 * which this width-only function can't see.
 */
internal fun monogramCoverMetrics(width: Dp): MonogramCoverMetrics {
    val borderInset = (width * BORDER_INSET_FRACTION).coerceIn(
        BORDER_INSET_FLOOR,
        BORDER_INSET_CEIL,
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
        showFullTitle = width >= FULL_TITLE_MIN_WIDTH,
        titleFontSize = titleFontSizeSp.sp,
        titleMaxLines = titleMaxLines,
    )
}
