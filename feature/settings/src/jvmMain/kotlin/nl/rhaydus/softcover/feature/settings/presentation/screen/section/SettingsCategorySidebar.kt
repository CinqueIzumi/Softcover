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
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.component.sidebar.Sidebar
import nl.rhaydus.softcover.core.component.sidebar.SidebarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.settings.presentation.action.OnSettingsCategorySelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.model.SettingsSidebarKey
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
    runAction: (SettingsAction) -> Unit,
    onProfileClick: () -> Unit,
    onHiddenSuggestionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onRowEvent: (SidebarEvent<SettingsSidebarKey>) -> Unit = onRowEvent@{ event ->
        val key = (event as? SidebarEvent.Clicked)?.key ?: return@onRowEvent

        when (key) {
            SettingsSidebarKey.Profile -> onProfileClick()
            SettingsSidebarKey.HiddenSuggestions -> onHiddenSuggestionsClick()
            is SettingsSidebarKey.Category -> runAction(OnSettingsCategorySelectedAction(category = key.category))
        }
    }

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = Spacing.md,
                    bottom = Spacing.md + rememberBottomBarPadding(),
                ),
        ) {
            PageMasthead(
                model = state.settingsSidebarMasthead,
                modifier = Modifier.padding(start = Spacing.gutter, end = Spacing.md),
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Sidebar(
                model = state.settingsSidebar,
                onEvent = onRowEvent,
            )
        }
    }
}
