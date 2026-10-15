package nl.rhaydus.softcover.feature.settings.presentation.util

internal actual fun settingsDisplayToggleAvailability() = SettingsDisplayToggleAvailability(
    showsFloatingBarToggle = true,
    showsShelfSwipeToggle = true,
    showsDynamicColorToggle = supportsDynamicColor(),
)
