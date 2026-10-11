package nl.rhaydus.softcover.core.component.chart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The bubble naming the active column, centred on [centerX] and clamped inside [rowWidth]. It reports a
 * zero size and sits wholly above its parent's top edge, so it never changes the chart's measured height.
 * It overlays whatever sits above the chart. A null [text] hides the bubble; it animates out on the last
 * text and centre, so a null [centerX] keeps the last centre.
 */
@Composable
internal fun ColumnReadout(
    text: String?,
    centerX: Float?,
    rowWidth: Float,
    modifier: Modifier = Modifier,
) {
    var lastText by remember { mutableStateOf("") }
    var lastCenterX by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(text) {
        if (text != null) lastText = text
    }

    LaunchedEffect(centerX) {
        if (centerX != null) lastCenterX = centerX
    }

    val decorative = playDecorativeMotion()
    val effects = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val spatial = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val gap = Spacing.xs

    Box(
        modifier = modifier.layout { measurable, _ ->
            val placeable = measurable.measure(
                Constraints(maxWidth = rowWidth.toInt().coerceAtLeast(0)),
            )
            val x = ((centerX ?: lastCenterX) - placeable.width / 2f)
                .coerceIn(
                    0f,
                    (rowWidth - placeable.width).coerceAtLeast(0f),
                )

            layout(width = 0, height = 0) {
                placeable.place(
                    IntOffset(
                        x = x.toInt(),
                        y = -placeable.height - gap.roundToPx(),
                    ),
                )
            }
        },
    ) {
        AnimatedVisibility(
            visible = text != null,
            enter = if (decorative) {
                fadeIn(effects) + scaleIn(
                    spatial,
                    initialScale = 0.9f,
                )
            } else {
                EnterTransition.None
            },
            exit = if (decorative) {
                fadeOut(effects) + scaleOut(
                    spatial,
                    targetScale = 0.9f,
                )
            } else {
                ExitTransition.None
            },
        ) {
            SoftcoverText(
                text = text ?: lastText,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = Corners.xs,
                    )
                    .padding(
                        horizontal = Spacing.xs,
                        vertical = Spacing.xxs,
                    ),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
