package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsCategory
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

/**
 * The category source list. Carries no version text of its own — the app version shows exactly once,
 * on the `About` pane (via
 * [AboutContent][nl.rhaydus.softcover.feature.settings.presentation.screen.AboutContent]'s
 * `VersionFooter`), not here alongside it.
 */
@Composable
internal fun SettingsCategorySidebar(
    state: SettingsScreenUiState,
    selected: SettingsCategory,
    onSelect: (SettingsCategory) -> Unit,
    onProfileClick: () -> Unit,
    onHiddenSuggestionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
        ) {
            PageMasthead(
                model = state.settingsSidebarMasthead,
                modifier = Modifier.padding(start = 26.dp, end = 16.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeader(
                model = state.accountSidebarLabel,
                modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 6.dp),
            )

            SettingsSidebarRow(
                label = "Your profile",
                icon = SoftcoverIcon.Account,
                selected = false,
                showTrailingArrow = true,
                onClick = onProfileClick,
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(
                model = state.personaliseSidebarLabel,
                modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 6.dp),
            )

            SettingsSidebarRow(
                label = "Appearance",
                icon = SoftcoverIcon.Palette,
                selected = selected == SettingsCategory.APPEARANCE,
                showTrailingArrow = false,
                onClick = { onSelect(SettingsCategory.APPEARANCE) },
            )

            SettingsSidebarRow(
                label = "Library tabs",
                icon = SoftcoverIcon.Shelf,
                selected = selected == SettingsCategory.LIBRARY_TABS,
                showTrailingArrow = false,
                onClick = { onSelect(SettingsCategory.LIBRARY_TABS) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(
                model = state.privacySidebarLabel,
                modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 6.dp),
            )

            SettingsSidebarRow(
                label = "Hidden suggestions",
                icon = SoftcoverIcon.FilterList,
                selected = false,
                showTrailingArrow = true,
                onClick = onHiddenSuggestionsClick,
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(
                model = state.aboutSidebarLabel,
                modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 6.dp),
            )

            SettingsSidebarRow(
                label = "About",
                icon = SoftcoverIcon.Settings,
                selected = selected == SettingsCategory.ABOUT,
                showTrailingArrow = false,
                onClick = { onSelect(SettingsCategory.ABOUT) },
            )

            SettingsSidebarRow(
                label = "Roadmap",
                icon = SoftcoverIcon.Explore,
                selected = selected == SettingsCategory.ROADMAP,
                showTrailingArrow = false,
                onClick = { onSelect(SettingsCategory.ROADMAP) },
            )
        }
    }
}
