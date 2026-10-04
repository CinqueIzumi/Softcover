package nl.rhaydus.softcover.core.component.statistic

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.text.SoftcoverText

private const val TABULAR_NUMS = "tnum"
private const val PULSE_DURATION_MS = 220

/**
 * The canonical way to render a numeric stat that can change in place — profile stats, pages-read
 * counters, deadline pacing. Raw `Text` for a number that can change should be treated as a bug.
 * Tweens between values when [StatNumberUiModel.value] changes.
 *
 * The display is locked to tabular figures so individual digits don't shift width mid-tween. When the
 * user has disabled system animations the number snaps — tweening here is decorative, the value
 * itself is the source of truth.
 *
 * Each integer crossing during the tween fires a brief 1dp hairline pulse under the number, tinted
 * with the number's own content colour so the tick reads against any surface — including a
 * `primary`-filled hero card, where that colour is `onPrimary` — a quiet ledger tick that makes a
 * count feel earned rather than rolled. Suppressed under reduced motion.
 *
 * It takes the `Text`-shaped render parameters after the model, the same way `RichText` does: a
 * number is type, and which face it is set in belongs to the surface rather than to the datum.
 * [style] / [autoSize] as loose parameters are an exception to components
 * taking only their model, event lambda and modifier, not a pattern to copy.
 */
@Composable
fun StatNumber(
    model: StatNumberUiModel,
    style: TextStyle,
    modifier: Modifier = Modifier,
    autoSize: TextAutoSize? = null,
) {
    val playMotion = playDecorativeMotion()

    val animated by animateFloatAsState(
        targetValue = model.value.toFloat(),
        label = "StatNumber",
    )

    val displayValue = if (playMotion) animated.toDouble() else model.value

    val integerKey = displayValue.toInt()

    val color = LocalContentColor.current

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
                color = color.copy(alpha = currentAlpha),
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
        SoftcoverText(
            text = remember(displayValue, model.format) { model.format.render(displayValue) },
            color = color,
            style = style.copy(fontFeatureSettings = TABULAR_NUMS),
            autoSize = autoSize,
            maxLines = 1,
            isLoading = model.isLoading,
            standIn = model.format.standIn(),
        )
    }
}

private fun StatNumberFormat.standIn(): String = when (this) {
    StatNumberFormat.Grouped -> "0,000"
    StatNumberFormat.Plain -> "00"
    StatNumberFormat.Decimal -> "0.0"
}

private fun StatNumberFormat.render(value: Double): String = when (this) {
    StatNumberFormat.Grouped -> formatGroupedNumber(value.toInt())
    StatNumberFormat.Plain -> value.toInt().toString()
    StatNumberFormat.Decimal -> formatDecimalNumber(
        value = value,
        fractionDigits = 1,
    )
}
