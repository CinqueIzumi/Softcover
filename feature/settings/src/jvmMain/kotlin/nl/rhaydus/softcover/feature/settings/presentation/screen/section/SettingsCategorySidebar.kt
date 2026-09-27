package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsCategory

/**
 * The category source list. Carries no version text of its own — the app version shows exactly once,
 * on the `About` pane (via
 * [AboutContent][nl.rhaydus.softcover.feature.settings.presentation.screen.AboutContent]'s
 * `VersionFooter`), not here alongside it.
 */
@Composable
internal fun SettingsCategorySidebar(
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
            SidebarHeader()

            Spacer(modifier = Modifier.height(20.dp))

            SidebarSectionLabel(text = "Account")

            SettingsSidebarRow(
                label = "Your profile",
                icon = SoftcoverIcon.Account,
                selected = false,
                showTrailingArrow = true,
                onClick = onProfileClick,
            )

            Spacer(modifier = Modifier.height(16.dp))

            SidebarSectionLabel(text = "Personalise")

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

            SidebarSectionLabel(text = "Privacy")

            SettingsSidebarRow(
                label = "Hidden suggestions",
                icon = SoftcoverIcon.FilterList,
                selected = false,
                showTrailingArrow = true,
                onClick = onHiddenSuggestionsClick,
            )

            Spacer(modifier = Modifier.height(16.dp))

            SidebarSectionLabel(text = "About")

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

@Composable
private fun SidebarHeader() {
    Column(modifier = Modifier.padding(start = 26.dp, end = 16.dp)) {
        Text(
            text = "Settings",
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tune Softcover to match how you read.",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SidebarSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrowSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(
            start = 26.dp,
            top = 8.dp,
            bottom = 6.dp,
        ),
    )
}
