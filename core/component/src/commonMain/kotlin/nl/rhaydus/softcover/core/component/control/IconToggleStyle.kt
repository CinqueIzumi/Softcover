package nl.rhaydus.softcover.core.component.control

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** What an icon [Toggle] paints for a [ToggleIconStyle] and its checked state. */
internal data class IconToggleStyle(
    val size: Dp,
    val shape: Shape,
    val container: Color,
    val tint: Color,
    val iconSize: Dp,
    val hasTooltip: Boolean,
)

internal fun ToggleIconStyle.style(
    scheme: ColorScheme,
    checked: Boolean,
): IconToggleStyle = when (this) {
    ToggleIconStyle.Filled -> IconToggleStyle(
        size = 44.dp,
        shape = RoundedCornerShape(12.dp),
        container = if (checked) scheme.primary else scheme.surfaceContainerHigh,
        tint = if (checked) scheme.onPrimary else scheme.primary,
        iconSize = 24.dp,
        hasTooltip = false,
    )

    ToggleIconStyle.Bare -> IconToggleStyle(
        size = 38.dp,
        shape = CircleShape,
        container = Color.Transparent,
        tint = if (checked) scheme.primary else scheme.outline,
        iconSize = 20.dp,
        hasTooltip = true,
    )
}
