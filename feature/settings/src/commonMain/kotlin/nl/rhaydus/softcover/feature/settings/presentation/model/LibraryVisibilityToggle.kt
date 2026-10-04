package nl.rhaydus.softcover.feature.settings.presentation.model

import nl.rhaydus.softcover.core.component.control.ToggleUiModel
import nl.rhaydus.softcover.core.component.control.ToggleVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/** The eye on a library-tab row: checked while the tab is shown, its description naming the next action. */
internal fun libraryVisibilityToggleFor(
    label: String,
    visible: Boolean,
): ToggleUiModel = ToggleUiModel(
    key = "library-tab-visibility",
    checked = visible,
    description = if (visible) "Hide $label from the library tabs" else "Show $label on the library tabs",
    variant = ToggleVariant.Bare,
    on = SoftcoverIcon.Visibility,
    off = SoftcoverIcon.VisibilityOff,
)
