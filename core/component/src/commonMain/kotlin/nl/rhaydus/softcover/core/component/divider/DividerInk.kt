package nl.rhaydus.softcover.core.component.divider

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/** A [Divider]'s colour, resolved from theme where it isn't a fixed on-image tint. */
sealed interface DividerInk {
    data object Outline : DividerInk

    data object ContentTint : DividerInk

    data object OnImage : DividerInk
}

internal fun DividerInk.resolve(
    colorScheme: ColorScheme,
    contentColor: Color,
): Color = when (this) {
    DividerInk.Outline -> colorScheme.outlineVariant
    DividerInk.ContentTint -> contentColor.copy(alpha = 0.25f)
    DividerInk.OnImage -> Color.White.copy(alpha = 0.3f)
}
