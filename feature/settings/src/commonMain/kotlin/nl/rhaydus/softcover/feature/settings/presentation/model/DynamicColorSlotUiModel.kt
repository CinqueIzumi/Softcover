package nl.rhaydus.softcover.feature.settings.presentation.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel

/** What the spine-colour section shows beneath its tiles in place of a fixed dynamic-colour switch. */
@Immutable
internal sealed interface DynamicColorSlotUiModel {
    /** The switch, on a platform that can take its scheme from the wallpaper. */
    data class Toggle(val row: ToggleRowUiModel<DisplayToggle>) : DynamicColorSlotUiModel

    /** A line of explanation standing where the switch would be. */
    data class Gloss(val text: String) : DynamicColorSlotUiModel

    /** Nothing: the platform has no dynamic colour to offer. */
    data object Hidden : DynamicColorSlotUiModel
}
