package nl.rhaydus.softcover.core.designsystem.presentation.component

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScale
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

private val TILE_SHAPE: Shape = RoundedCornerShape(14.dp)
private const val TILE_ASPECT_RATIO = 0.78f

/**
 * The tile chrome every Appearance picker choice shares: a fixed-aspect, clipped, ringed miniature
 * over its [label]. [content] paints the miniature itself and is clipped to the tile's shape.
 *
 * It is shared by [ThemePreviewTile] and [ColorPalettePreviewTile] so the two pickers read as one
 * row of the same object rather than as two lookalikes that drift apart. It stays internal to the
 * design system — a feature composes the finished tiles, never the frame.
 *
 * Selection is carried by a `primary` ring (2dp, against 1dp `outlineVariant` at rest) plus the
 * `primary` label beneath — no check badge, since the ring already says it once — and by
 * [Role.RadioButton] semantics for the reader who can see neither. Press feedback is `pressScale` on
 * the tile's own interaction source, with `pointerHandCursor` for desktop.
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

    val accentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Column(
        modifier = modifier
            .pointerHandCursor()
            .pressScale(interactionSource)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(TILE_ASPECT_RATIO)
                .clip(TILE_SHAPE)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = accentColor,
                    shape = TILE_SHAPE,
                ),
            content = content,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = label,
            style = MaterialTheme.editorialTypography.titleSmall,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
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
            .clip(RoundedCornerShape(2.dp))
            .background(color),
    )
}
