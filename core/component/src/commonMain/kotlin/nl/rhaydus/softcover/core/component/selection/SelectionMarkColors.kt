package nl.rhaydus.softcover.core.component.selection

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/** [SelectionMark]'s resolved container and content colours. */
internal data class SelectionMarkColors(
    val container: Color,
    val content: Color,
) {
    companion object {
        fun of(
            selected: Boolean,
            backdrop: SelectionMarkBackdrop,
            scheme: ColorScheme,
        ): SelectionMarkColors = if (selected) {
            SelectionMarkColors(
                container = scheme.primary,
                content = scheme.onPrimary,
            )
        } else {
            SelectionMarkColors(
                container = unselectedFill(
                    backdrop = backdrop,
                    scheme = scheme,
                ),
                content = Color.Unspecified,
            )
        }

        private fun unselectedFill(
            backdrop: SelectionMarkBackdrop,
            scheme: ColorScheme,
        ): Color = when (backdrop) {
            SelectionMarkBackdrop.Image -> scheme.surface.copy(alpha = UNSELECTED_IMAGE_ALPHA)
            SelectionMarkBackdrop.Surface -> scheme.surfaceContainerHigh
        }

        private const val UNSELECTED_IMAGE_ALPHA = 0.72f
    }
}
