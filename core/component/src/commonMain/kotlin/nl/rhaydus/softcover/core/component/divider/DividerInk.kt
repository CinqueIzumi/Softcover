package nl.rhaydus.softcover.core.component.divider

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/** A [Divider]'s colour, resolved from theme where it isn't a fixed on-image tint. */
sealed interface DividerInk {
    data object Outline : DividerInk

    data class ContentTint(val alpha: Float) : DividerInk

    data class OnImage(val alpha: Float) : DividerInk
}

internal fun DividerInk.resolve(
    colorScheme: ColorScheme,
    contentColor: Color,
): Color = when (this) {
    DividerInk.Outline -> colorScheme.outlineVariant
    is DividerInk.ContentTint -> contentColor.copy(alpha = alpha)
    is DividerInk.OnImage -> Color.White.copy(alpha = alpha)
}
