package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import nl.rhaydus.softcover.core.presentation.util.playDecorativeMotion

private const val TABULAR_NUMS = "tnum"

/**
 * Renders a numeric stat that tweens between values when [value] changes.
 *
 * The display is locked to tabular figures so individual digits don't shift width
 * mid-tween. When the user has disabled system animations the number snaps —
 * tweening here is decorative, the value itself is the source of truth.
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

    Text(
        text = formatted,
        modifier = modifier,
        color = resolvedColor,
        style = style.copy(fontFeatureSettings = TABULAR_NUMS),
        autoSize = autoSize,
        maxLines = maxLines,
    )
}

/**
 * Float-valued variant for stats with fractional precision (e.g. an average rating).
 * The [formatter] is responsible for rounding/locale formatting; the tween still
 * runs on the raw [value], so partial-digit motion remains smooth.
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

    Text(
        text = formatted,
        modifier = modifier,
        color = resolvedColor,
        style = style.copy(fontFeatureSettings = TABULAR_NUMS),
        autoSize = autoSize,
        maxLines = maxLines,
    )
}

private fun Color.takeOrUnspecified(fallback: Color): Color =
    if (this == Color.Unspecified) fallback else this
