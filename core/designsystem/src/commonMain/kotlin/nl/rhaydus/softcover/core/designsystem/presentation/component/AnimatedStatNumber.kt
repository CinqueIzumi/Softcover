package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion

private const val TABULAR_NUMS = "tnum"
private const val PULSE_DURATION_MS = 220

/**
 * Renders a numeric stat that tweens between values when [value] changes.
 *
 * The display is locked to tabular figures so individual digits don't shift width
 * mid-tween. When the user has disabled system animations the number snaps —
 * tweening here is decorative, the value itself is the source of truth.
 *
 * Each integer crossing during the tween fires a brief 1dp primary-tinted hairline pulse
 * under the number — a quiet ledger-style tick that makes the count feel earned rather
 * than rolled. Suppressed under reduced motion.
 */
@Composable
fun AnimatedStatNumber(
    value: Int,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    formatter: (Int) -> String = Int::toString,
    autoSize: TextAutoSize? = null,
    maxLines: Int = 1,
) {
    val playMotion = playDecorativeMotion()

    val animated by animateFloatAsState(
        targetValue = value.toFloat(),
        label = "AnimatedStatNumber",
    )

    val displayValue = if (playMotion) animated.toInt() else value

    val formatted = remember(displayValue) { formatter(displayValue) }

    val resolvedColor = color.takeOrUnspecified(LocalContentColor.current)

    StatPulseText(
        text = formatted,
        integerKey = displayValue,
        style = style,
        color = resolvedColor,
        modifier = modifier,
        autoSize = autoSize,
        maxLines = maxLines,
    )
}

/**
 * Float-valued variant for stats with fractional precision (e.g. an average rating).
 * The [formatter] is responsible for rounding/locale formatting; the tween still
 * runs on the raw [value], so partial-digit motion remains smooth. The hairline pulse
 * fires on each integer crossing of the tweened value.
 */
@Composable
fun AnimatedStatNumber(
    value: Float,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    formatter: (Float) -> String,
    autoSize: TextAutoSize? = null,
    maxLines: Int = 1,
) {
    val playMotion = playDecorativeMotion()

    val animated by animateFloatAsState(
        targetValue = value,
        label = "AnimatedStatNumber",
    )

    val displayValue = if (playMotion) animated else value

    val formatted = remember(displayValue) { formatter(displayValue) }

    val resolvedColor = color.takeOrUnspecified(LocalContentColor.current)

    StatPulseText(
        text = formatted,
        integerKey = displayValue.toInt(),
        style = style,
        color = resolvedColor,
        modifier = modifier,
        autoSize = autoSize,
        maxLines = maxLines,
    )
}

@Composable
private fun StatPulseText(
    text: String,
    integerKey: Int,
    style: TextStyle,
    color: Color,
    modifier: Modifier,
    autoSize: TextAutoSize?,
    maxLines: Int,
) {
    val playMotion = playDecorativeMotion()

    val pulseColor = color

    val hairlinePx = with(LocalDensity.current) { 1.dp.toPx() }

    val pulse = remember { Animatable(initialValue = 0f) }

    var firstRun by remember { mutableStateOf(true) }

    LaunchedEffect(integerKey) {
        if (firstRun) {
            firstRun = false
            return@LaunchedEffect
        }

        if (playMotion.not()) return@LaunchedEffect

        pulse.snapTo(targetValue = 1f)
        pulse.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = PULSE_DURATION_MS),
        )
    }

    Box(
        modifier = modifier.drawBehind {
            val currentAlpha = pulse.value

            if (currentAlpha <= 0f) return@drawBehind

            drawRect(
                color = pulseColor.copy(alpha = currentAlpha),
                topLeft = Offset(
                    x = 0f,
                    y = size.height - hairlinePx,
                ),
                size = Size(
                    width = size.width,
                    height = hairlinePx,
                ),
            )
        },
    ) {
        Text(
            text = text,
            color = color,
            style = style.copy(fontFeatureSettings = TABULAR_NUMS),
            autoSize = autoSize,
            maxLines = maxLines,
        )
    }
}

private fun Color.takeOrUnspecified(fallback: Color): Color =
    if (this == Color.Unspecified) fallback else this
