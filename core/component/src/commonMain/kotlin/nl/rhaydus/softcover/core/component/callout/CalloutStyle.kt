package nl.rhaydus.softcover.core.component.callout

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import nl.rhaydus.designsystem.model.ButtonStyle

@Immutable
internal data class CalloutStyle(
    val container: Color,
    val content: Color,
    val mutedInk: Color,
    val buttonStyle: ButtonStyle,
)

internal fun calloutStyle(
    tone: CalloutTone,
    scheme: ColorScheme,
): CalloutStyle = when (tone) {
    CalloutTone.Neutral -> CalloutStyle(
        container = scheme.surfaceContainerHigh,
        content = scheme.onSurface,
        mutedInk = scheme.onSurfaceVariant,
        buttonStyle = ButtonStyle.TONAL,
    )

    CalloutTone.Highlight -> CalloutStyle(
        container = scheme.primaryContainer,
        content = scheme.onPrimaryContainer,
        mutedInk = scheme.onPrimaryContainer.copy(alpha = 0.8f),
        buttonStyle = ButtonStyle.FILLED,
    )
}
