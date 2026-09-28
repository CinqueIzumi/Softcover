package nl.rhaydus.softcover.core.component.badge

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/** [BadgeTone]'s resolved container and content colours. */
internal data class BadgeColors(
    val container: Color,
    val content: Color,
) {
    companion object {
        fun of(
            tone: BadgeTone,
            scheme: ColorScheme,
        ): BadgeColors = when (tone) {
            BadgeTone.Primary -> BadgeColors(
                container = scheme.primary,
                content = scheme.onPrimary,
            )

            BadgeTone.PrimaryContainer -> BadgeColors(
                container = scheme.primaryContainer,
                content = scheme.onPrimaryContainer,
            )

            BadgeTone.ErrorContainer -> BadgeColors(
                container = scheme.errorContainer,
                content = scheme.onErrorContainer,
            )

            BadgeTone.Neutral -> BadgeColors(
                container = scheme.surfaceContainerHigh,
                content = scheme.onSurfaceVariant,
            )

            BadgeTone.Scrim -> BadgeColors(
                container = Color.Black.copy(alpha = SCRIM_ALPHA),
                content = Color.White,
            )
        }

        private const val SCRIM_ALPHA = 0.55f
    }
}
