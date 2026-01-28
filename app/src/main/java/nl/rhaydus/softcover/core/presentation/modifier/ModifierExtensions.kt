package nl.rhaydus.softcover.core.presentation.modifier

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline

@Composable
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    return this.clickable(
        onClick = onClick,
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    )
}

@Composable
fun Modifier.shimmer(shape: Shape = RectangleShape, isLoading: Boolean): Modifier {
    return if (isLoading) this.shimmer(shape = shape) else this
}

@Composable
fun Modifier.shimmer(shape: Shape = RectangleShape): Modifier {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.White.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition(label = "Shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1600,
                easing = FastOutSlowInEasing,
            )
        ),
        label = "Translate"
    )

    return this.drawWithCache {
        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim, 0f),
            // wider gradient
            end = Offset(translateAnim + size.width / 1.5f, size.height)
        )

        val outline = shape.createOutline(
            size = size,
            layoutDirection = layoutDirection,
            density = this
        )

        onDrawWithContent {
            drawOutline(outline, brush = brush)
        }
    }
}

@Composable
fun Modifier.conditional(
    condition: Boolean,
    ifTrue: @Composable () -> Modifier,
    ifFalse: @Composable () -> Modifier = { Modifier },
): Modifier {
    return if (condition) {
        then(ifTrue())
    } else {
        then(ifFalse())
    }
}