package nl.rhaydus.softcover.core.component.control

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

@Immutable
sealed interface ToggleVariant {
    /** A glyph that swaps between [on] and [off]. */
    data class Icon(
        val on: SoftcoverIcon,
        val off: SoftcoverIcon,
        val style: ToggleIconStyle,
    ) : ToggleVariant

    /** The platform switch; the visible label stays with the caller. */
    data object Switch : ToggleVariant
}
