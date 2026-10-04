package nl.rhaydus.softcover.core.component.control

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/** What one Prose word paints for its selected state; the underline is transparent when unchosen. */
internal data class ProseWordColors(
    val word: Color,
    val underline: Color,
)

internal fun proseWordColors(
    scheme: ColorScheme,
    selected: Boolean,
): ProseWordColors = if (selected) {
    ProseWordColors(
        word = scheme.primary,
        underline = scheme.primary,
    )
} else {
    ProseWordColors(
        word = scheme.onSurfaceVariant.copy(alpha = 0.55f),
        underline = Color.Transparent,
    )
}
