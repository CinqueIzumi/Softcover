package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.RoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.AboutPane
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.AppearancePane
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.LibraryTabsPane
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.RoadmapPane
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.SettingsCategorySidebar
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

/**
 * Desktop renders the bespoke master–detail Settings surface.
 */
internal actual val settingsUsesMasterDetail: Boolean = true

/**
 * Desktop Settings: a category **source list** down the leading edge (the native desktop settings
 * idiom) beside a detail pane that swaps between Appearance, Library tabs, and About **inline** — no
 * push, no full-page swap. Mirrors desktop Library's `[ sidebar | content ]` shape. "Your profile" is
 * the one entry that still pushes full-screen (Profile is a separate feature). The Appearance pane
 * drives the shared [settingsRunAction]; the Library-tabs pane drives [libraryVisibilityState] /
 * [libraryVisibilityRunAction] (its model is hosted under the Settings lifecycle in
 * [SettingsScreen.Content]) and docks a
 * [LibraryVisibilitySaveBar][nl.rhaydus.softcover.feature.settings.presentation.screen.section.LibraryVisibilitySaveBar]
 * at the pane's bottom; the About pane drives [openUrl] and its own `Roadmap` row (which selects the
 * `ROADMAP` category rather than pushing); the Roadmap pane drives [roadmapState] / [roadmapRunAction]
 * (its model is likewise hosted under the Settings lifecycle). The sub-page `navigateTo*` callbacks
 * (including [navigateToAbout] and [navigateToRoadmap]) are unused here (mobile pushes; desktop swaps) —
 * only [navigateToProfile], [navigateToHiddenSuggestions], and [navigateToComponentGallery] are wired,
 * the last one into the `About` pane's own version-footer easter egg (`component-contract.md` § 7.5).
 */
@Composable
internal actual fun SettingsScreenLayout(
    state: SettingsScreenUiState,
    settingsRunAction: (SettingsAction) -> Unit,
    navigateToProfile: () -> Unit,
    navigateToAppearanceSettings: () -> Unit,
    navigateToLibraryVisibility: () -> Unit,
    navigateToHiddenSuggestions: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToRoadmap: () -> Unit,
    navigateToComponentGallery: () -> Unit,
    libraryVisibilityState: LibraryVisibilitySettingsUiState,
    libraryVisibilityRunAction: (LibraryVisibilityAction) -> Unit,
    roadmapState: RoadmapUiState,
    roadmapRunAction: (RoadmapAction) -> Unit,
    onCreateListClick: () -> Unit,
    appUpdateState: AppUpdateState,
    onStartAppUpdate: () -> Unit,
    openUrl: (String) -> Unit,
    debugSection: @Composable () -> Unit,
) {
    var selected by remember { mutableStateOf(SettingsCategory.APPEARANCE) }

    Row(modifier = Modifier.fillMaxSize()) {
        SettingsCategorySidebar(
            state = state,
            selected = selected,
            onSelect = { selected = it },
            onProfileClick = navigateToProfile,
            onHiddenSuggestionsClick = navigateToHiddenSuggestions,
            modifier = Modifier
                .width(SETTINGS_SIDEBAR_WIDTH)
                .fillMaxHeight(),
        )

        VerticalDivider()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            when (selected) {
                SettingsCategory.APPEARANCE -> AppearancePane(
                    state = state,
                    runAction = settingsRunAction,
                )

                SettingsCategory.LIBRARY_TABS -> LibraryTabsPane(
                    state = libraryVisibilityState,
                    runAction = libraryVisibilityRunAction,
                    onCreateListClick = onCreateListClick,
                )

                SettingsCategory.ABOUT -> AboutPane(
                    masthead = state.aboutPaneMasthead,
                    versionName = state.appVersionName,
                    versionCode = state.appVersionCode,
                    appUpdateState = appUpdateState,
                    onStartAppUpdate = onStartAppUpdate,
                    openUrl = openUrl,
                    onRoadmapClick = { selected = SettingsCategory.ROADMAP },
                    onComponentGalleryUnlocked = navigateToComponentGallery,
                    debugSection = debugSection,
                )

                SettingsCategory.ROADMAP -> RoadmapPane(
                    state = roadmapState,
                    runAction = roadmapRunAction,
                    openUrl = openUrl,
                )
            }
        }
    }
}

private val SETTINGS_SIDEBAR_WIDTH = 240.dp
