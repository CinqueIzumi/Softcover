package nl.rhaydus.softcover.showcase.live.sidebar.mapper

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowVariant
import nl.rhaydus.softcover.core.component.sidebar.SidebarSectionUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.showcase.live.sidebar.state.SidebarLiveUiState

private const val APPEARANCE = "appearance"
private const val LIBRARY_TABS = "library-tabs"
private const val READING_GOALS = "reading-goals"
private const val PROFILE = "profile"
private const val CURRENTLY_READING = "currently-reading"

internal fun sidebarLiveState(): SidebarLiveUiState = SidebarLiveUiState(
    topBar = TopBarUiModel(title = "Sidebar"),
    sidebar = sidebarModel(selected = APPEARANCE),
)

internal fun sidebarModel(selected: String): SidebarUiModel<String> = SidebarUiModel(
    sections = persistentListOf(
        SidebarSectionUiModel(
            label = SectionHeaderUiModel.Label(eyebrow = "Personalisation"),
            rows = persistentListOf(
                destination(
                    key = APPEARANCE,
                    label = "Appearance",
                    icon = SoftcoverIcon.Palette,
                    selected = selected,
                ),
                destination(
                    key = LIBRARY_TABS,
                    label = "Library tabs",
                    icon = SoftcoverIcon.LibraryBooks,
                    selected = selected,
                ),
                destination(
                    key = READING_GOALS,
                    label = "Reading goals",
                    icon = SoftcoverIcon.Reading,
                    selected = selected,
                ),
            ),
        ),
        SidebarSectionUiModel(
            label = SectionHeaderUiModel.Label(eyebrow = "Your shelves"),
            rows = persistentListOf(
                destination(
                    key = PROFILE,
                    label = "Your profile",
                    icon = SoftcoverIcon.Account,
                    selected = selected,
                ),
                destination(
                    key = CURRENTLY_READING,
                    label = "Currently reading",
                    icon = SoftcoverIcon.MenuBook,
                    selected = selected,
                    longPressable = true,
                ),
            ),
        ),
    ),
)

private fun destination(
    key: String,
    label: String,
    icon: SoftcoverIcon,
    selected: String,
    longPressable: Boolean = false,
): SidebarRowUiModel<String> = SidebarRowUiModel(
    key = key,
    label = label,
    variant = SidebarRowVariant.Destination(
        selected = key == selected,
        longPressable = longPressable,
    ),
    icon = icon,
)
