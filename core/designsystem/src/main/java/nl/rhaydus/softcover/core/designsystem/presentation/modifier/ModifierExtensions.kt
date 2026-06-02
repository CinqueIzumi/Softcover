package nl.rhaydus.softcover.core.designsystem.presentation.modifier

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import nl.rhaydus.softcover.core.designsystem.presentation.util.playDecorativeMotion

private const val PRESS_SCALE_TARGET = 0.97f

@Composable
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    return this.clickable(
        onClick = onClick,
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
    )
}

/**
 * Scales the receiver down to ~0.97 while the [interactionSource] reports a press, easing
 * back to 1f on release. Suppressed when system animations are disabled — under reduced
 * motion the surface stays at its natural size.
 *
 * Pair with the call site's existing click handler; this modifier only renders the visual
 * press feedback and does not attach a click listener of its own. For ripple-less card and
 * hero-cover surfaces, prefer [pressScaleClickable] which bundles both.
 */
@Composable
fun Modifier.pressScale(interactionSource: InteractionSource): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val playMotion = playDecorativeMotion()
    val target = if (isPressed && playMotion) PRESS_SCALE_TARGET else 1f

    val scale by animateFloatAsState(
        targetValue = target,
        label = "pressScale",
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Ripple-less clickable that also plays the press-scale feedback. The default replacement
 * for [noRippleClickable] on cards and ripple-less hero surfaces (book detail cover) — the
 * scale stops removing the indication from meaning "remove the feedback".
 */
@Composable
fun Modifier.pressScaleClickable(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }

    return this
        .pressScale(interactionSource)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
}

/**
 * Sibling to [pressScaleClickable] that also accepts an optional long-press handler. Reach for
 * this when a surface needs the same ripple-less press-scale feedback but also has to react to a
 * long-press (e.g. long-press to enter library bulk-select mode). The long-press handler must
 * fire its own haptic — this modifier deliberately does not, so callers can pick the right
 * semantic (`threshold` for selection entry, `lift` for drag pickup, etc.).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.pressScaleCombinedClickable(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }

    return this
        .pressScale(interactionSource)
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
            onLongClick = onLongClick,
        )
}

@Composable
fun Modifier.shimmer(
    shape: Shape = RectangleShape,
    isLoading: Boolean,
): Modifier {
    return if (isLoading) this.shimmer(shape = shape) else this
}

@Composable
fun Modifier.shimmer(shape: Shape = RectangleShape): Modifier {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.White.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.3f),
    )

    val transition = rememberInfiniteTransition(label = "Shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1600,
                easing = FastOutSlowInEasing,
            ),
        ),
        label = "Translate",
    )

    return this.drawWithCache {
        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(
                translateAnim,
                0f,
            ),
            // wider gradient
            end = Offset(
                translateAnim + size.width / 1.5f,
                size.height,
            ),
        )

        val outline = shape.createOutline(
            size = size,
            layoutDirection = layoutDirection,
            density = this,
        )

        onDrawWithContent {
            drawOutline(
                outline,
                brush = brush,
            )
        }
    }
}

fun Modifier.grayscale(): Modifier {
    val paint = Paint().apply {
        colorFilter = ColorFilter.colorMatrix(
            ColorMatrix().apply { setToSaturation(0f) },
        )
    }

    return this.drawWithContent {
        drawIntoCanvas { canvas ->
            canvas.saveLayer(
                bounds = Rect(
                    Offset.Zero,
                    size,
                ),
                paint = paint,
            )
            drawContent()
            canvas.restore()
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