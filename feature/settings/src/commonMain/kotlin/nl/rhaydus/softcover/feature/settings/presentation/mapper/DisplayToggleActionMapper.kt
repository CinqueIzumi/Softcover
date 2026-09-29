package nl.rhaydus.softcover.feature.settings.presentation.mapper

import nl.rhaydus.softcover.feature.settings.presentation.action.OnDynamicColorToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnFloatingBarToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnReadingStreakToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnShelfSwipeToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.model.DisplayToggle

/** Resolves a [DisplayToggle] row's key to the action that persists it, shared by the Display and spine-colour sections. */
internal fun displayToggleActionFor(
    key: DisplayToggle,
    newValue: Boolean,
): SettingsAction = when (key) {
    DisplayToggle.FLOATING_BOTTOM_BAR -> OnFloatingBarToggledAction(newValue = newValue)
    DisplayToggle.SHELF_SWIPE -> OnShelfSwipeToggledAction(newValue = newValue)
    DisplayToggle.READING_STREAK -> OnReadingStreakToggledAction(newValue = newValue)
    DisplayToggle.DYNAMIC_COLOR -> OnDynamicColorToggledAction(newValue = newValue)
}
