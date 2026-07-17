package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.math.floor
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * A book without a resolvable cover, rendered as a filled tile carrying its [title] in the editorial
 * Fraunces voice rather than a blank tile. This is [EditionImage]'s final fallback rung — reached when
 * no cover source resolves, or a known URL fails to load, and the data is not still loading. A cover
 * slot is never blank: every non-decorative call site reaches this component through that fallback
 * rung (the tag-editor jacket and the explore hidden-suggestions shelf thumbnails included), rather
 * than instantiating it directly.
 *
 * **Size-adaptive.** A [BoxWithConstraints] measures the tile the caller gives it and derives padding,
 * the autosize font floor/ceiling, and the line-count ceiling purely from that measurement (see
 * [coverlessTitleMetrics]), so one component serves everything from a 48dp shelf thumbnail up through
 * the widest real library grid tile (~183dp on a large phone; 200dp+ only occurs on wide desktop
 * panes) with no per-call-site tuning. Line height is set as an **em** value —
 * relative to the resolved font size — rather than inherited from the base style's fixed `sp` value, so
 * the line box shrinks in lockstep with the autosized glyphs instead of leaving oversized gaps once the
 * text has been fitted down to a small size. [Hyphens.None] paired with [LineBreak.Heading] makes the
 * autosizer prefer shrinking the whole block over splitting a word mid-way.
 *
 * Imagery cards keep a container shade (DS §2.1) — this never reaches for `primary`.
 *
 * Imposes **no aspect ratio of its own**; it fills whatever box [modifier] and the surrounding layout
 * give it. [EditionImage] applies the 2:3 cover aspect on its own container before falling back here.
 */
@Composable
fun CoverlessTitleCover(
    title: String,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        val baseStyle = MaterialTheme.editorialTypography.headlineMedium
        val metrics = coverlessTitleMetrics(
            width = maxWidth,
            height = maxHeight,
            baseFontSize = baseStyle.fontSize,
        )

        Text(
            text = title,
            style = baseStyle.copy(
                lineHeight = LINE_HEIGHT_EM.em,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                ),
                hyphens = Hyphens.None,
                lineBreak = LineBreak.Heading,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            autoSize = TextAutoSize.StepBased(
                minFontSize = metrics.minFontSize,
                maxFontSize = metrics.maxFontSize,
                stepSize = 1.sp,
            ),
            maxLines = metrics.maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = metrics.horizontalPadding,
                    vertical = metrics.verticalPadding,
                ),
        )
    }
}

private const val HORIZONTAL_PADDING_FRACTION = 0.08f
private const val VERTICAL_PADDING_FRACTION = 0.10f
private const val MIN_FONT_SIZE_FRACTION = 0.10f
private const val MIN_FONT_SIZE_FLOOR_SP = 7f
private const val MAX_FONT_SIZE_FRACTION = 0.28f
private const val LINE_HEIGHT_EM = 1.15f
private const val MAX_LINES_FLOOR = 1
private const val MAX_LINES_CAP = 5

private val HORIZONTAL_PADDING_FLOOR = 4.dp
private val HORIZONTAL_PADDING_CAP = 12.dp
private val VERTICAL_PADDING_FLOOR = 4.dp
private val VERTICAL_PADDING_CAP = 16.dp

internal data class CoverlessTitleMetrics(
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val minFontSize: TextUnit,
    val maxFontSize: TextUnit,
    val maxLines: Int,
)

/**
 * Derives [CoverlessTitleCover]'s size-adaptive fit purely from the measured tile [width] and
 * [height], so the component needs no per-call-site tuning across everything from a 48dp shelf
 * thumbnail up through the widest real library grid tile (~183dp on a large phone; 200dp+ only occurs
 * on wide desktop panes). [baseFontSize] is the editorial headline face's own size,
 * the ceiling the autosize search is never allowed to exceed. Pure (non-composable) so the fit math is
 * independently testable.
 *
 * - Padding scales with [width] (today's flat 12dp/16dp is the cap, so the large grid tile is
 *   unchanged), floored so it never vanishes on a thumbnail-sized tile.
 * - The autosize floor scales with [width] instead of the old flat 12sp, so a 48dp thumbnail can settle
 *   on a genuinely small size rather than fighting a floor sized for a much larger tile.
 * - The autosize ceiling is [baseFontSize], capped by tile size, so a small thumbnail doesn't start its
 *   step search at the full headline size only to immediately shrink.
 * - `maxLines` is the available text height (tile height, minus the derived vertical padding on both
 *   edges) divided by the worst-case (smallest-font) line height, so a narrow tile never gets a
 *   line-count budget sized for a wide one.
 */
internal fun coverlessTitleMetrics(
    width: Dp,
    height: Dp,
    baseFontSize: TextUnit,
): CoverlessTitleMetrics {
    val horizontalPadding = (width * HORIZONTAL_PADDING_FRACTION)
        .coerceIn(
            HORIZONTAL_PADDING_FLOOR,
            HORIZONTAL_PADDING_CAP,
        )
    val verticalPadding = (width * VERTICAL_PADDING_FRACTION)
        .coerceIn(
            VERTICAL_PADDING_FLOOR,
            VERTICAL_PADDING_CAP,
        )

    val minFontSizeSp = (width.value * MIN_FONT_SIZE_FRACTION)
        .coerceAtLeast(MIN_FONT_SIZE_FLOOR_SP)
    val maxFontSizeSp = (width.value * MAX_FONT_SIZE_FRACTION)
        .coerceAtMost(baseFontSize.value)
        .coerceAtLeast(minFontSizeSp)

    val textHeight = (height - verticalPadding * 2).coerceAtLeast(0.dp)
    val worstCaseLineHeightSp = minFontSizeSp * LINE_HEIGHT_EM
    val maxLines = floor(textHeight.value / worstCaseLineHeightSp)
        .toInt()
        .coerceIn(
            MAX_LINES_FLOOR,
            MAX_LINES_CAP,
        )

    return CoverlessTitleMetrics(
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        minFontSize = minFontSizeSp.sp,
        maxFontSize = maxFontSizeSp.sp,
        maxLines = maxLines,
    )
}

@StandardPreview
@Composable
private fun CoverlessTitleCoverPreview() {
    SoftcoverTheme {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CoverlessTitleCover(
                    title = "It",
                    modifier = Modifier
                        .width(48.dp)
                        .aspectRatio(2f / 3f),
                )

                CoverlessTitleCover(
                    title = "The Very Long and Overwrought Title of a Book That Will Not Fit",
                    modifier = Modifier
                        .width(48.dp)
                        .aspectRatio(2f / 3f),
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CoverlessTitleCover(
                    title = "It",
                    modifier = Modifier
                        .width(160.dp)
                        .aspectRatio(2f / 3f),
                )

                CoverlessTitleCover(
                    title = "The Very Long and Overwrought Title of a Book That Will Not Fit",
                    modifier = Modifier
                        .width(160.dp)
                        .aspectRatio(2f / 3f),
                )
            }
        }
    }
}
