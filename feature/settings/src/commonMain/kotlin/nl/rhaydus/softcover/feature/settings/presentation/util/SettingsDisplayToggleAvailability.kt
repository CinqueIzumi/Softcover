package nl.rhaydus.softcover.feature.settings.presentation.util

/** Which platform-gated Appearance controls this platform shows. */
internal data class SettingsDisplayToggleAvailability(
    val showsFloatingBarToggle: Boolean,
    val showsShelfSwipeToggle: Boolean,
    val showsDynamicColorToggle: Boolean,
)

internal expect fun settingsDisplayToggleAvailability(): SettingsDisplayToggleAvailability
