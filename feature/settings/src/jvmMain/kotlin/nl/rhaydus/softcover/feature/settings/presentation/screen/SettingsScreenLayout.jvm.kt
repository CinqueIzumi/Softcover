package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.divider.VerticalDivider
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.component.layout.DesktopScrollPage
import nl.rhaydus.softcover.core.presentation.util.TabReselectScrollToTopEffect
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnSaveLibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnSettingsCategorySelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.RoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.AboutPane
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.LibraryVisibilitySaveBar
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.SettingsCategorySidebar
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

internal actual val settingsUsesMasterDetail: Boolean = true

/**
 * Desktop Settings: a category source list beside a detail pane that swaps categories inline. Only
 * Profile, Hidden suggestions and the Component Gallery push a screen; the other `navigateTo*`
 * callbacks are unused here.
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
    onStartAppUpdate: () -> Unit,
    openUrl: (String) -> Unit,
    debugSection: @Composable () -> Unit,
) {
    val selected = state.selectedCategory

    // Fresh per category, as the pages' own state was before it was hoisted for the reselect effect.
    val pageScrollState = remember(selected) { ScrollState(initial = 0) }

    TabReselectScrollToTopEffect(
        tab = SettingsTab,
        state = pageScrollState,
    )

    Row(modifier = Modifier.fillMaxSize()) {
        SettingsCategorySidebar(
            state = state,
            runAction = settingsRunAction,
            onProfileClick = navigateToProfile,
            onHiddenSuggestionsClick = navigateToHiddenSuggestions,
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight(),
        )

        VerticalDivider()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            when (selected) {
                SettingsCategory.APPEARANCE -> DesktopScrollPage(
                    scrollState = pageScrollState,
                    masthead = { PageMasthead(model = state.appearancePaneMasthead) },
                ) {
                    AppearanceSettingsContent(
                        state = state,
                        runAction = settingsRunAction,
                        showUiScaleControl = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                SettingsCategory.LIBRARY_TABS -> DesktopScrollPage(
                    scrollState = pageScrollState,
                    bottomBar = {
                        LibraryVisibilitySaveBar(
                            isDirty = libraryVisibilityState.isDirty,
                            isSaving = libraryVisibilityState.isSaving,
                            onSave = { libraryVisibilityRunAction(OnSaveLibraryVisibilityAction()) },
                        )
                    },
                ) {
                    LibraryVisibilityContent(
                        state = libraryVisibilityState,
                        runAction = libraryVisibilityRunAction,
                        onCreateListClick = onCreateListClick,
                    )
                }

                SettingsCategory.ABOUT -> DesktopScrollPage(
                    scrollState = pageScrollState,
                    masthead = { PageMasthead(model = state.aboutPaneMasthead) },
                ) {
                    AboutContent(
                        versionLabel = state.versionLabel,
                        creditsHeader = state.aboutCreditsHeader,
                        sourceHeader = state.aboutSourceHeader,
                        contactHeader = state.aboutContactHeader,
                        openUrl = openUrl,
                        onRoadmapClick = {
                            settingsRunAction(OnSettingsCategorySelectedAction(category = SettingsCategory.ROADMAP))
                        },
                        onComponentGalleryUnlocked = navigateToComponentGallery,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    AboutPane(
                        appUpdateCallout = state.appUpdateCallout,
                        onStartAppUpdate = onStartAppUpdate,
                        debugSection = debugSection,
                    )
                }

                SettingsCategory.ROADMAP -> DesktopScrollPage(
                    scrollState = pageScrollState,
                    masthead = { PageMasthead(model = roadmapState.masthead) },
                ) {
                    RoadmapContent(
                        state = roadmapState,
                        runAction = roadmapRunAction,
                        openUrl = openUrl,
                    )
                }
            }
        }
    }
}
