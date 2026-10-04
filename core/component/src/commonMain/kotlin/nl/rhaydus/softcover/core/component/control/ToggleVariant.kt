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

    /** The platform switch; its row text stays with the caller. */
    data object Switch : ToggleVariant

    /** A switch that only shows [ToggleUiModel.checked]: it takes no input and has no semantics, so a surrounding row owns the tap. */
    data object DisplaySwitch : ToggleVariant
}
