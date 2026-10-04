package nl.rhaydus.softcover.core.component.control

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/** What one Track segment paints for its selected state. */
internal data class SegmentColors(
    val container: Color,
    val content: Color,
)

internal fun segmentColors(
    scheme: ColorScheme,
    selected: Boolean,
): SegmentColors = if (selected) {
    SegmentColors(
        container = scheme.primary,
        content = scheme.onPrimary,
    )
} else {
    SegmentColors(
        container = Color.Transparent,
        content = scheme.onSurfaceVariant,
    )
}
