package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.icon.RhaydusIconResource
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.modifier.pressScale
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.RoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
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
 * with its own model (and, for About and Roadmap, their own `LocalUriHandler`). [state] is likewise
 * unused: it carried nothing this menu itself renders once the app version moved off this list and onto
 * [AboutScreen] — its sole home now — to avoid showing the version in two places. [navigateToRoadmap]
 * *is* used — this menu's own direct "Roadmap" shortcut, alongside the desktop sidebar's equivalent row
 * (`SettingsScreenLayout.jvm.kt`'s `SettingsCategorySidebar`) — even though the same screen is also
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
    appUpdateState: AppUpdateState,
    onStartAppUpdate: () -> Unit,
    openUrl: (String) -> Unit,
    debugSection: @Composable () -> Unit,
) {
    val bottomBarPadding = rememberBottomBarPadding()

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .cappedContentWidth()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsPageHeader()

            Spacer(modifier = Modifier.height(32.dp))

            if (appUpdateState != AppUpdateState.Idle) {
                AppUpdateSection(
                    appUpdateState = appUpdateState,
                    onClick = onStartAppUpdate,
                )

                Spacer(modifier = Modifier.height(40.dp))
            }

            EditorialSectionHeader(
                eyebrow = "Account",
                headline = "Your reader",
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsMenuRow(
                title = "View user profile",
                gloss = "Your shelves, stats and reading year",
                icon = drawableIconResource(
                    icon = SoftcoverIcon.Account,
                    contentDescription = "Account icon",
                ),
                onClick = navigateToProfile,
            )

            Spacer(modifier = Modifier.height(40.dp))

            EditorialSectionHeader(
                eyebrow = "Personalisation",
                headline = "Make it yours",
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsMenuRow(
                title = "Appearance",
                gloss = "Theme, accent and text size",
                icon = drawableIconResource(
                    icon = SoftcoverIcon.Palette,
                    contentDescription = "Appearance icon",
                ),
                onClick = navigateToAppearanceSettings,
            )

            SettingsMenuRow(
                title = "Library tabs",
                gloss = "Which shelves show, and their order",
                icon = drawableIconResource(
                    icon = SoftcoverIcon.Shelf,
                    contentDescription = "Library tabs icon",
                ),
                onClick = navigateToLibraryVisibility,
            )

            SettingsMenuRow(
                title = "Hidden suggestions",
                gloss = "Books you've asked us to stop recommending",
                icon = drawableIconResource(
                    icon = SoftcoverIcon.FilterList,
                    contentDescription = "Hidden suggestions icon",
                ),
                onClick = navigateToHiddenSuggestions,
            )

            Spacer(modifier = Modifier.height(40.dp))

            EditorialSectionHeader(
                eyebrow = "About",
                headline = "The fine print",
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsMenuRow(
                title = "About Softcover",
                gloss = "Credits, source, and how to reach us",
                icon = drawableIconResource(
                    icon = SoftcoverIcon.Info,
                    contentDescription = "About icon",
                ),
                onClick = navigateToAbout,
            )

            SettingsMenuRow(
                title = "Roadmap",
                gloss = "What we're building next, in the order we plan to ship it",
                icon = drawableIconResource(
                    icon = SoftcoverIcon.Explore,
                    contentDescription = "Roadmap icon",
                ),
                onClick = navigateToRoadmap,
            )

            Spacer(modifier = Modifier.height(40.dp))

            debugSection()

            Spacer(modifier = Modifier.height(24.dp + bottomBarPadding))
        }
    }
}

@Composable
private fun SettingsPageHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Settings",
            style = MaterialTheme.editorialTypography.pageTitle,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Tune Softcover to match how you read.",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(max = 300.dp),
        )
    }
}

/**
 * The Settings menu's borderless row: a top hairline (drawn on every row, including the first, so
 * rows read as a continuous hairline-separated list rather than a boxed card), a `surfaceContainerHigh`
 * icon tile, an Inter [title] (never italic) over an italic Fraunces [gloss], and a demoted trailing
 * chevron. Pressing washes the row to `surfaceContainer` — the row's only container tint.
 */
@Composable
private fun SettingsMenuRow(
    title: String,
    gloss: String,
    icon: RhaydusIconResource,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val playMotion = playDecorativeMotion()

    val rowBackground by animateColorAsState(
        targetValue = if (isPressed && playMotion) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            Color.Transparent
        },
        label = "settingsMenuRowPress",
    )

    val hairlineColor = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .background(rowBackground)
            .drawBehind {
                drawLine(
                    color = hairlineColor,
                    start = Offset(
                        x = 0f,
                        y = 0f,
                    ),
                    end = Offset(
                        x = size.width,
                        y = 0f,
                    ),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(
                horizontal = 4.dp,
                vertical = 17.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon.getIconPainter(),
                contentDescription = icon.contentDescription,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = gloss,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        val arrowIcon = drawableIconResource(
            icon = SoftcoverIcon.KeyboardArrowRight,
            contentDescription = "",
        )

        Icon(
            painter = arrowIcon.getIconPainter(),
            contentDescription = arrowIcon.contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
            appUpdateState = AppUpdateState.Idle,
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
            appUpdateState = AppUpdateState.Available,
            onStartAppUpdate = {},
            openUrl = {},
            debugSection = {},
        )
    }
}
