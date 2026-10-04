package nl.rhaydus.softcover.feature.settings.presentation.mapper

import nl.rhaydus.softcover.feature.settings.presentation.model.DisplayToggle
import nl.rhaydus.softcover.feature.settings.presentation.model.SettingsToggleRowUiModel
import nl.rhaydus.softcover.feature.settings.presentation.model.settingsSwitchToggle

/** The spine-colour picker's dynamic-colour row, off the composition (`component-contract.md` R9). */
internal fun dynamicColorToggleRowFor(checked: Boolean): SettingsToggleRowUiModel<DisplayToggle> = SettingsToggleRowUiModel(
    key = DisplayToggle.DYNAMIC_COLOR,
    label = "Dynamic colour",
    gloss = "Take the whole scheme from your wallpaper instead of a spine colour.",
    toggle = settingsSwitchToggle(
        key = DisplayToggle.DYNAMIC_COLOR.name,
        label = "Dynamic colour",
        checked = checked,
    ),
)
