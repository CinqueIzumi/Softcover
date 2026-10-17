package nl.rhaydus.softcover.feature.settings.presentation.model

import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsCategory

internal sealed interface SettingsSidebarKey {
    data object Profile : SettingsSidebarKey
    data object HiddenSuggestions : SettingsSidebarKey
    data class Category(val category: SettingsCategory) : SettingsSidebarKey
}
