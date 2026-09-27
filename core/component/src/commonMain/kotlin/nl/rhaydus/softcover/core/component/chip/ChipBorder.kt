package nl.rhaydus.softcover.core.component.chip

import androidx.compose.ui.graphics.Color

/** The outline [ChipStyle] paints, drawn at [ChipDimensions.borderWidth]. */
internal sealed interface ChipBorder {
    data object None : ChipBorder
    data class Solid(val color: Color) : ChipBorder
    data class Dashed(val color: Color) : ChipBorder
}
