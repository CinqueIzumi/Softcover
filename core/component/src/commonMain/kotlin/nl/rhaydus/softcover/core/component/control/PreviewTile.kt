package nl.rhaydus.softcover.core.component.control

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.softcover.core.component.modifier.tactileSelectable
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

private const val TILE_ASPECT_RATIO = 0.78f

/**
 * The tile chrome every Appearance picker choice shares: a fixed-aspect, clipped, ringed miniature
 * over its [label]. [content] paints the miniature itself and is clipped to the tile's shape.
 *
 * It is shared by [ThemePreviewTile] and [ColorPalettePreviewTile] so the two pickers read as one
 * row of the same object rather than as two lookalikes that drift apart. It stays internal to the
 * component library — a feature composes the finished tiles, never the frame.
 *
 * Selection is carried by a `primary` ring (2dp, against 1dp `outlineVariant` at rest) plus the
 * `primary` label beneath — no check badge, since the ring already says it once — and by
 * [Role.RadioButton] semantics for the reader who can see neither.
 */
@Composable
internal fun PreviewTileFrame(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val motionScheme = MaterialTheme.motionScheme
    val accentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = motionScheme.defaultEffectsSpec<Color>(),
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 2.dp else 1.dp,
        animationSpec = motionScheme.defaultEffectsSpec<Dp>(),
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = motionScheme.defaultEffectsSpec<Color>(),
    )

    Column(
        modifier = modifier
            .tactileSelectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
                hoverShape = null,
                interactionSource = interactionSource,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(TILE_ASPECT_RATIO)
                .clip(Corners.lg)
                .hoverHighlight(
                    interactionSource = interactionSource,
                    shape = Corners.lg,
                )
                .border(
                    width = borderWidth,
                    color = accentColor,
                    shape = Corners.lg,
                ),
            content = content,
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        SoftcoverText(
            text = label,
            style = MaterialTheme.editorialTypography.titleSmall,
            color = labelColor,
            textAlign = TextAlign.Center,
        )
    }
}

/** One rounded rule inside a miniature, standing in for a run of type — or for the accent bar. */
@Composable
internal fun MiniBar(
    widthFraction: Float,
    height: Dp,
    color: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(Corners.xxs)
            .background(color),
    )
}
