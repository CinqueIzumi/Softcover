package nl.rhaydus.softcover.core.component.cover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MonogramCoverForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MonogramCoverInk
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * A book without a resolvable cover, rendered as a fixed-ink **monogram jacket** — a dark
 * [MonogramCoverInk] panel with an inset hairline border and the [title] set large in italic
 * Fraunces, centred and wrapped across lines — rather than a blank tile. This is [Cover]'s
 * final fallback rung, reached when no cover source resolves, or a known URL fails to load, and the
 * data is not still loading. A cover slot is never blank: every non-decorative call site reaches
 * this component through that fallback rung (the tag-editor jacket and the explore
 * hidden-suggestions shelf thumbnails included), rather than instantiating it directly.
 *
 * **Keyed purely on "no art exists," never on release status.** This is the app-wide coverless
 * fallback — a book (released or unreleased) with real cover art always shows that art; the
 * monogram only ever stands in for a genuinely missing cover.
 *
 * **Size-adaptive, with a graceful degrade to a single initial.** A [BoxWithConstraints] measures
 * the tile the caller gives it and derives the border inset, the full-title rendering's starting
 * font size / line cap, and the single-initial fallback's font size purely from that measurement
 * (see [monogramCoverMetrics]). At or above [monogramCoverMetrics]'s width floor, the whole [title]
 * renders via `TextAutoSize`, which shrinks the font further (down to a legible minimum) to fit the
 * tile's own width, ellipsizing if it still overflows the line cap; below that floor — a tile too
 * small to fit even a couple of legible words — it falls back to [title]'s first letter set large,
 * the original single-initial treatment. One component therefore serves everything from a 48dp
 * shelf thumbnail (initial only) up through the widest real library grid tile (full title) with no
 * per-call-site tuning.
 *
 * [MonogramCoverInk] / [MonogramCoverForeground] are fixed printed-jacket inks (design-system.md
 * §2.1) — they read identically in light and dark, unlike a themed container shade.
 *
 * Imposes **no aspect ratio of its own**; it fills whatever box [modifier] and the surrounding
 * layout give it. [Cover] applies the 2:3 cover aspect on its own container before falling
 * back here.
 */
@Composable
internal fun CoverlessTitleCover(
    model: CoverUiModel,
    modifier: Modifier = Modifier,
) {
    val title = model.coverlessTitle.orEmpty()
    val trimmedTitle = title.trim()
    val initial = trimmedTitle.firstOrNull()?.uppercase().orEmpty()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MonogramCoverInk),
        contentAlignment = Alignment.Center,
    ) {
        val metrics = monogramCoverMetrics(width = maxWidth)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(metrics.borderInset)
                .border(
                    width = 1.dp,
                    color = MonogramCoverForeground.copy(alpha = 0.24f),
                    shape = Corners.xxs,
                ),
        )

        if (metrics.showFullTitle && trimmedTitle.isNotEmpty()) {
            SoftcoverText(
                text = trimmedTitle,
                style = MaterialTheme.editorialTypography.headlineMedium,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = MONOGRAM_MIN_LEGIBLE_FONT_SIZE,
                    maxFontSize = metrics.titleFontSize,
                    stepSize = 1.sp,
                ),
                maxLines = metrics.titleMaxLines,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = MonogramCoverForeground,
                modifier = Modifier.padding(horizontal = metrics.borderInset + Spacing.xxs),
            )
        } else if (initial.isNotEmpty()) {
            SoftcoverText(
                text = initial,
                style = MaterialTheme.editorialTypography.headlineMedium.copy(
                    fontSize = metrics.initialFontSize,
                    lineHeight = metrics.initialFontSize,
                ),
                color = MonogramCoverForeground,
            )
        }
    }
}
