package nl.rhaydus.softcover.feature.settings.presentation.mapper

import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowVariant
import nl.rhaydus.softcover.feature.settings.presentation.model.DisplayToggle

/** The spine-colour picker's dynamic-colour row, built off the composition. */
internal fun dynamicColorToggleRowFor(checked: Boolean): ToggleRowUiModel<DisplayToggle> = ToggleRowUiModel(
    key = DisplayToggle.DYNAMIC_COLOR,
    title = "Dynamic colour",
    gloss = "Take the whole scheme from your wallpaper instead of a spine colour.",
    checked = checked,
    variant = ToggleRowVariant.Inset,
)
