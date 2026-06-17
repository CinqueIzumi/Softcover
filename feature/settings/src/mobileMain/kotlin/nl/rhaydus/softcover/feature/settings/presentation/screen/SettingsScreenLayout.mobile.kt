package nl.rhaydus.softcover.feature.settings.presentation.screen

import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.icon.RhaydusIconResource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.modifier.noRippleClickable
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.util.rememberBottomBarPadding
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

/**
 * Mobile keeps the master–detail off; Appearance and Library tabs are pushed as their own screens.
 */
internal actual val settingsUsesMasterDetail: Boolean = false

/**
 * The mobile Settings menu pushes its sub-pages, so the desktop-only master–detail parameters
 * ([settingsRunAction], [libraryVisibilityState], [libraryVisibilityRunAction], [onCreateListClick])
 * are unused here — the toggles live on the pushed [AppearanceSettingsScreen] /
 * [LibraryVisibilitySettingsScreen], each with its own model.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun SettingsScreenLayout(
    state: SettingsScreenUiState,
    settingsRunAction: (SettingsAction) -> Unit,
    navigateToProfile: () -> Unit,
    navigateToAppearanceSettings: () -> Unit,
    navigateToLibraryVisibility: () -> Unit,
    libraryVisibilityState: LibraryVisibilitySettingsUiState,
    libraryVisibilityRunAction: (LibraryVisibilityAction) -> Unit,
    onCreateListClick: () -> Unit,
    appUpdateState: AppUpdateState,
    onStartAppUpdate: () -> Unit,
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

            SettingsGroup {
                SettingsRow(
                    title = "View user profile",
                    icon = drawableIconResource(
                        icon = SoftcoverIcon.Account,
                        contentDescription = "Account icon",
                    ),
                    onClick = navigateToProfile,
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            EditorialSectionHeader(
                eyebrow = "Personalisation",
                headline = "Make it yours",
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsGroup {
                SettingsRow(
                    title = "Appearance",
                    icon = drawableIconResource(
                        icon = SoftcoverIcon.Palette,
                        contentDescription = "Appearance icon",
                    ),
                    onClick = navigateToAppearanceSettings,
                )

                SettingsRowDivider()

                SettingsRow(
                    title = "Library tabs",
                    icon = drawableIconResource(
                        icon = SoftcoverIcon.Shelf,
                        contentDescription = "Library tabs icon",
                    ),
                    onClick = navigateToLibraryVisibility,
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            debugSection()

            VersionFooter(
                versionName = state.appVersionName,
                versionCode = state.appVersionCode,
            )

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
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    icon: RhaydusIconResource,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable(onClick)
            .padding(
                horizontal = 20.dp,
                vertical = 18.dp,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = icon.getIconPainter(),
                contentDescription = icon.contentDescription,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = title,
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

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
            libraryVisibilityState = LibraryVisibilitySettingsUiState(),
            libraryVisibilityRunAction = {},
            onCreateListClick = {},
            appUpdateState = AppUpdateState.Idle,
            onStartAppUpdate = {},
            debugSection = {},
        )
    }
}
