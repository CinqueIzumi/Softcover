package nl.rhaydus.softcover.feature.settings.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowVariant
import nl.rhaydus.softcover.core.component.sidebar.SidebarSectionUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.feature.settings.presentation.model.SettingsSidebarKey
import nl.rhaydus.softcover.feature.settings.presentation.model.SettingsSidebarSections
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsCategory

internal fun settingsSidebarSectionsFor(selectedCategory: SettingsCategory): SettingsSidebarSections = SettingsSidebarSections(
    account = SidebarSectionUiModel(
        label = SectionHeaderUiModel.Label(eyebrow = "Account"),
        rows = persistentListOf(
            SidebarRowUiModel(
                key = SettingsSidebarKey.Profile,
                label = "Your profile",
                icon = SoftcoverIcon.Account,
                variant = SidebarRowVariant.Push,
            ),
        ),
    ),
    personalise = SidebarSectionUiModel(
        label = SectionHeaderUiModel.Label(eyebrow = "Personalise"),
        rows = persistentListOf(
            SidebarRowUiModel(
                key = SettingsSidebarKey.Category(category = SettingsCategory.APPEARANCE),
                label = "Appearance",
                icon = SoftcoverIcon.Palette,
                variant = SidebarRowVariant.Destination(selected = selectedCategory == SettingsCategory.APPEARANCE),
            ),
            SidebarRowUiModel(
                key = SettingsSidebarKey.Category(category = SettingsCategory.LIBRARY_TABS),
                label = "Library tabs",
                icon = SoftcoverIcon.Shelf,
                variant = SidebarRowVariant.Destination(selected = selectedCategory == SettingsCategory.LIBRARY_TABS),
            ),
        ),
    ),
    privacy = SidebarSectionUiModel(
        label = SectionHeaderUiModel.Label(eyebrow = "Privacy"),
        rows = persistentListOf(
            SidebarRowUiModel(
                key = SettingsSidebarKey.HiddenSuggestions,
                label = "Hidden suggestions",
                icon = SoftcoverIcon.FilterList,
                variant = SidebarRowVariant.Push,
            ),
        ),
    ),
    about = SidebarSectionUiModel(
        label = SectionHeaderUiModel.Label(eyebrow = "About"),
        rows = persistentListOf(
            SidebarRowUiModel(
                key = SettingsSidebarKey.Category(category = SettingsCategory.ABOUT),
                label = "About",
                icon = SoftcoverIcon.Settings,
                variant = SidebarRowVariant.Destination(selected = selectedCategory == SettingsCategory.ABOUT),
            ),
            SidebarRowUiModel(
                key = SettingsSidebarKey.Category(category = SettingsCategory.ROADMAP),
                label = "Roadmap",
                icon = SoftcoverIcon.Explore,
                variant = SidebarRowVariant.Destination(selected = selectedCategory == SettingsCategory.ROADMAP),
            ),
        ),
    ),
)
