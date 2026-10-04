package nl.rhaydus.softcover.core.component.button

import androidx.compose.ui.graphics.Color

/** What [PillButton] paints for a [PillButtonTone]. */
internal data class PillButtonStyle(
    val container: Color,
    val ink: Color,
    val iconTint: Color = ink,
    val border: Color? = null,
)
