package nl.rhaydus.softcover.feature.settings.presentation.util

/** Which mobile-only Display toggles this platform shows. */
internal data class SettingsDisplayToggleAvailability(
    val showsFloatingBarToggle: Boolean,
    val showsShelfSwipeToggle: Boolean,
)

internal expect fun settingsDisplayToggleAvailability(): SettingsDisplayToggleAvailability
