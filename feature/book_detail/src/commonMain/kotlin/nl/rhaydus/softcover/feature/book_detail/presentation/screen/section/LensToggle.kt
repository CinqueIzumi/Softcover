package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLens

/**
 * The sticky lens toggle (design-system.md's lens-toggle pattern): a full-width segmented pill on a
 * `surfaceContainer` track that switches the sections below between "Yours" and "The Book". Both
 * segments are always visible; "Yours" is disabled (dimmed, non-interactive) until the book has a
 * user copy. The active segment crossfades over ~220ms, gated by [playDecorativeMotion]; a `select`
 * haptic fires on switch. The caller composes this as a `stickyHeader` under the top bar.
 */
@Composable
internal fun LensToggle(
    selectedLens: BookDetailLens,
    onLensSelected: (BookDetailLens) -> Unit,
    yoursEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberHaptics()

    // The caller composes this as a `stickyHeader`, so the root paints an opaque, edge-to-edge
    // page-background layer first — otherwise content scrolling underneath would show through the
    // padded pill's own margin while the header is pinned.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(percent = 50),
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                LensSegment(
                    label = "The Book",
                    selected = selectedLens == BookDetailLens.THE_BOOK,
                    enabled = true,
                    onClick = {
                        haptics.select()
                        onLensSelected(BookDetailLens.THE_BOOK)
                    },
                    modifier = Modifier.weight(1f),
                )

                LensSegment(
                    label = "Yours",
                    selected = selectedLens == BookDetailLens.YOURS,
                    enabled = yoursEnabled,
                    onClick = {
                        haptics.select()
                        onLensSelected(BookDetailLens.YOURS)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun LensSegment(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playMotion = playDecorativeMotion()
    val colorSpec = if (playMotion) tween<Color>(durationMillis = 220) else snap()

    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = colorSpec,
        label = "LensSegmentContainer",
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = colorSpec,
        label = "LensSegmentContent",
    )

    // "Yours" flips enabled the instant a shelved book lands (t1 of the load choreography) — an
    // un-animated alpha snap reads as a flash amid everything else settling at once, so this rides
    // the same color-spec tween/snap gate as the selection crossfade above.
    val floatSpec = if (playMotion) tween<Float>(durationMillis = 220) else snap()
    val disabledAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.4f,
        animationSpec = floatSpec,
        label = "LensSegmentEnabledAlpha",
    )

    Surface(
        modifier = modifier.alpha(disabledAlpha),
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(percent = 50),
        onClick = onClick,
        enabled = enabled && selected.not(),
    ) {
        Box(
            modifier = Modifier.padding(vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}
