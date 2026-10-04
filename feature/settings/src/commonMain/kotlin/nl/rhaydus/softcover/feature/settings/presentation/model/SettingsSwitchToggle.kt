package nl.rhaydus.softcover.feature.settings.presentation.model

import nl.rhaydus.softcover.core.component.control.ToggleUiModel
import nl.rhaydus.softcover.core.component.control.ToggleVariant

internal fun settingsSwitchToggle(
    key: String,
    label: String,
    checked: Boolean,
): ToggleUiModel = ToggleUiModel(
    key = key,
    checked = checked,
    description = label,
    variant = ToggleVariant.DisplaySwitch,
)
