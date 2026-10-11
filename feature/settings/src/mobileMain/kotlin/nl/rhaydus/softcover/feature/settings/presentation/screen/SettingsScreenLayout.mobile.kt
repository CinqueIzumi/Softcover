package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.callout.Callout
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.core.presentation.util.TabReselectScrollToTopEffect
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.RoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.mapper.appUpdateCalloutFor
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.SettingsMenuRow
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.SettingsMenuRowTarget
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.SettingsRowGroup
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

/**
 * Mobile keeps the master–detail off; Appearance and Library tabs are pushed as their own screens.
 */
internal actual val settingsUsesMasterDetail: Boolean = false

/**
 * The mobile Settings menu pushes its sub-pages, so the desktop-only master–detail parameters
 * ([settingsRunAction], [libraryVisibilityState], [libraryVisibilityRunAction], [roadmapState],
 * [roadmapRunAction], [onCreateListClick], [openUrl]) are unused here — the toggles live on the pushed
 * [AppearanceSettingsScreen] / [LibraryVisibilitySettingsScreen] / [AboutScreen] / [RoadmapScreen], each
 * with its own model (and, for About and Roadmap, their own `LocalUriHandler`). [state] now carries only
 * this menu's own page masthead — the app version moved off this list and onto [AboutScreen], its sole
 * home now, to avoid showing the version in two places. [navigateToRoadmap]
 * *is* used — this menu's own direct "Roadmap" shortcut, alongside the desktop sidebar's equivalent row
 * (`section/SettingsCategorySidebar.kt`'s `SettingsCategorySidebar`) — even though the same screen is also
 * reachable a second way, via the row [AboutContent] renders once you're already on About.
 * [navigateToComponentGallery] is unused here, the mirror image of [navigateToRoadmap]'s desktop
 * story: mobile reaches the Component Gallery only through the version footer's easter egg on the
 * pushed [AboutScreen], which wires that gesture to its own navigator directly rather than through
 * this layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    val bottomBarPadding = rememberBottomBarPadding()
    val scrollState = rememberScrollState()

    TabReselectScrollToTopEffect(
        tab = SettingsTab,
        state = scrollState,
    )

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .cappedContentWidth()
                .padding(horizontal = Spacing.gutter),
        ) {
            Spacer(modifier = Modifier.height(Spacing.xs))

            PageMasthead(model = state.settingsPageMasthead)

            Spacer(modifier = Modifier.height(Spacing.xxxl))

            state.appUpdateCallout?.let { appUpdateCallout ->
                Callout(
                    model = appUpdateCallout,
                    onEvent = { event ->
                        handleAppUpdateCalloutEvent(
                            event = event,
                            onStartAppUpdate = onStartAppUpdate,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(Spacing.section))
            }

            SectionHeader(model = state.settingsMenuAccountHeader)

            Spacer(modifier = Modifier.height(Spacing.md))

            SettingsRowGroup {
                row {
                    SettingsMenuRow(
                        title = "Your profile",
                        gloss = "Your shelves, stats and reading year",
                        icon = SoftcoverIcon.Account,
                        target = SettingsMenuRowTarget.SubPage(onClick = navigateToProfile),
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.section))

            SectionHeader(model = state.settingsMenuPersonalisationHeader)

            Spacer(modifier = Modifier.height(Spacing.md))

            SettingsRowGroup {
                row {
                    SettingsMenuRow(
                        title = "Appearance",
                        gloss = "Theme, accent and text size",
                        icon = SoftcoverIcon.Palette,
                        target = SettingsMenuRowTarget.SubPage(onClick = navigateToAppearanceSettings),
                    )
                }
                row {
                    SettingsMenuRow(
                        title = "Library tabs",
                        gloss = "Which shelves show, and their order",
                        icon = SoftcoverIcon.Shelf,
                        target = SettingsMenuRowTarget.SubPage(onClick = navigateToLibraryVisibility),
                    )
                }
                row {
                    SettingsMenuRow(
                        title = "Hidden suggestions",
                        gloss = "Books you've asked us to stop recommending",
                        icon = SoftcoverIcon.FilterList,
                        target = SettingsMenuRowTarget.SubPage(onClick = navigateToHiddenSuggestions),
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.section))

            SectionHeader(model = state.settingsMenuAboutHeader)

            Spacer(modifier = Modifier.height(Spacing.md))

            SettingsRowGroup {
                row {
                    SettingsMenuRow(
                        title = "About Softcover",
                        gloss = "Credits, source, and how to reach us",
                        icon = SoftcoverIcon.Info,
                        target = SettingsMenuRowTarget.SubPage(onClick = navigateToAbout),
                    )
                }
                row {
                    SettingsMenuRow(
                        title = "Roadmap",
                        gloss = "What we're building next, in the order we plan to ship it",
                        icon = SoftcoverIcon.Explore,
                        target = SettingsMenuRowTarget.SubPage(onClick = navigateToRoadmap),
                    )
                }
            }

            debugSection()

            Spacer(modifier = Modifier.height(Spacing.section + bottomBarPadding))
        }
    }
}

@StandardPreview
@Composable
private fun SettingsScreenPreview() {
    SoftcoverTheme {
        SettingsScreenLayout(
            state = SettingsScreenUiState(),
            settingsRunAction = {},
            navigateToProfile = {},
            navigateToAppearanceSettings = {},
            navigateToLibraryVisibility = {},
            navigateToHiddenSuggestions = {},
            navigateToAbout = {},
            navigateToRoadmap = {},
            navigateToComponentGallery = {},
            libraryVisibilityState = LibraryVisibilitySettingsUiState(),
            libraryVisibilityRunAction = {},
            roadmapState = RoadmapUiState(),
            roadmapRunAction = {},
            onCreateListClick = {},
            onStartAppUpdate = {},
            openUrl = {},
            debugSection = {},
        )
    }
}

@StandardPreview
@Composable
private fun SettingsScreenUpdateAvailablePreview() {
    SoftcoverTheme {
        SettingsScreenLayout(
            state = SettingsScreenUiState(appUpdateCallout = appUpdateCalloutFor(state = AppUpdateState.Available)),
            settingsRunAction = {},
            navigateToProfile = {},
            navigateToAppearanceSettings = {},
            navigateToLibraryVisibility = {},
            navigateToHiddenSuggestions = {},
            navigateToAbout = {},
            navigateToRoadmap = {},
            navigateToComponentGallery = {},
            libraryVisibilityState = LibraryVisibilitySettingsUiState(),
            libraryVisibilityRunAction = {},
            roadmapState = RoadmapUiState(),
            roadmapRunAction = {},
            onCreateListClick = {},
            onStartAppUpdate = {},
            openUrl = {},
            debugSection = {},
        )
    }
}
