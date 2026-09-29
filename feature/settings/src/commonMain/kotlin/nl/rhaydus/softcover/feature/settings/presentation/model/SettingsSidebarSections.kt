package nl.rhaydus.softcover.feature.settings.presentation.model

import nl.rhaydus.softcover.core.component.sidebar.SidebarSectionUiModel

internal data class SettingsSidebarSections(
    val account: SidebarSectionUiModel<SettingsSidebarKey>,
    val personalise: SidebarSectionUiModel<SettingsSidebarKey>,
    val privacy: SidebarSectionUiModel<SettingsSidebarKey>,
    val about: SidebarSectionUiModel<SettingsSidebarKey>,
)
