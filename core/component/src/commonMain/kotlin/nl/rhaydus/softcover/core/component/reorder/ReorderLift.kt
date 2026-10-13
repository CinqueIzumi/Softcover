package nl.rhaydus.softcover.core.component.reorder

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners

private const val LIFTED_SCALE = 1.04f

/**
 * The shared "picked up" treatment for a drag-to-reorder item: while [isDragging] it scales up and sits
 * on a raised plate with a shadow, and settles back on drop. The plate and shadow reach [outset] past the
 * item's bounds; everything is drawn, not measured, so layout never shifts.
 */
@Composable
fun Modifier.reorderLift(
    isDragging: Boolean,
    outset: Dp = 0.dp,
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isDragging) LIFTED_SCALE else 1f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "reorderLiftScale",
    )

    val lift by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
        label = "reorderLiftPlate",
    )

    val plateColor = MaterialTheme.colorScheme.surfaceContainer
    val plateShape = remember(outset) { OutsetShape(outset) }

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            shadowElevation = 8.dp.toPx() * lift
            shape = plateShape
            clip = false
        }
        .background(
            color = plateColor.copy(alpha = lift),
            shape = plateShape,
        )
}

private class OutsetShape(
    private val outset: Dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val outsetPx = with(density) { outset.toPx() }

        val grownSize = Size(
            width = size.width + 2 * outsetPx,
            height = size.height + 2 * outsetPx,
        )

        return Outline.Rounded(
            RoundRect(
                left = -outsetPx,
                top = -outsetPx,
                right = size.width + outsetPx,
                bottom = size.height + outsetPx,
                cornerRadius = CornerRadius(
                    Corners.md.topStart.toPx(
                        shapeSize = grownSize,
                        density = density,
                    ),
                ),
            ),
        )
    }
}
