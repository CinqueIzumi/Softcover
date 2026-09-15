package nl.rhaydus.softcover.core.component.statistic

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
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.designsystem.motion.playDecorativeMotion

private const val TABULAR_NUMS = "tnum"
private const val PULSE_DURATION_MS = 220

/**
 * Renders a numeric stat that tweens between values when [StatNumberUiModel.value] changes.
 *
 * The display is locked to tabular figures so individual digits don't shift width mid-tween. When the
 * user has disabled system animations the number snaps — tweening here is decorative, the value
 * itself is the source of truth.
 *
 * Each integer crossing during the tween fires a brief 1dp hairline pulse under the number, tinted
 * with the number's own content colour so the tick reads against any surface — a quiet ledger tick
 * that makes a count feel earned rather than rolled. Suppressed under reduced motion.
 *
 * It takes the `Text`-shaped render parameters after the model, the same way `RichText` does: a
 * number is type, and which face it is set in belongs to the surface rather than to the datum.
 */
@Composable
fun StatNumber(
    model: StatNumberUiModel,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    autoSize: TextAutoSize? = null,
    maxLines: Int = 1,
) {
    val playMotion = playDecorativeMotion()

    val animated by animateFloatAsState(
        targetValue = model.value.toFloat(),
        label = "StatNumber",
    )

    val displayValue = if (playMotion) animated.toDouble() else model.value

    val formatted = remember(displayValue, model.format) { model.format.render(displayValue) }

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

private fun StatNumberFormat.render(value: Double): String = when (this) {
    StatNumberFormat.Grouped -> formatGroupedNumber(value.toInt())
    StatNumberFormat.Plain -> value.toInt().toString()

    is StatNumberFormat.Decimal -> formatDecimalNumber(
        value = value,
        fractionDigits = fractionDigits,
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
