package nl.rhaydus.softcover.feature.settings.presentation.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.designsystem.R
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.designsystem.presentation.debug.DebugRoutesSection
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.noRippleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.util.LocalAppUpdateState
import nl.rhaydus.softcover.core.designsystem.presentation.util.LocalStartAppUpdate
import nl.rhaydus.softcover.core.designsystem.presentation.util.rememberBottomBarPadding
import nl.rhaydus.softcover.core.domain.appupdate.AppUpdateSimulator
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import kotlinx.coroutines.launch

object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<SettingsScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val navigator = LocalNavigator.currentOrThrow
        val appNavigator = koinInject<AppNavigator>()
        val appUpdateState = LocalAppUpdateState.current
        val onStartAppUpdate = LocalStartAppUpdate.current

        Screen(
            state = state,
            runAction = screenModel::runAction,
            navigateToProfile = {
                navigator.parent?.push(appNavigator.screen(ScreenDestination.Profile))
            },
            navigateToAppearanceSettings = {
                navigator.parent?.push(AppearanceSettingsScreen())
            },
            navigateToLibraryVisibility = {
                navigator.parent?.push(LibraryVisibilitySettingsScreen())
            },
            appUpdateState = appUpdateState,
            onStartAppUpdate = onStartAppUpdate,
            debugSection = {
                DebugRoutesSection()

                AppUpdateSimulatorSection(simulator = koinInject())
            },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    internal fun Screen(
        state: SettingsScreenUiState,
        runAction: (SettingsAction) -> Unit,
        navigateToProfile: () -> Unit,
        navigateToAppearanceSettings: () -> Unit,
        navigateToLibraryVisibility: () -> Unit,
        appUpdateState: AppUpdateState = AppUpdateState.Idle,
        onStartAppUpdate: () -> Unit = {},
        debugSection: @Composable () -> Unit = {},
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
                        icon = SoftcoverIconResource.Drawable(
                            id = R.drawable.ic_account,
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
                        icon = SoftcoverIconResource.Drawable(
                            id = R.drawable.ic_palette,
                            contentDescription = "Appearance icon",
                        ),
                        onClick = navigateToAppearanceSettings,
                    )

                    SettingsRowDivider()

                    SettingsRow(
                        title = "Library tabs",
                        icon = SoftcoverIconResource.Drawable(
                            id = R.drawable.ic_shelf,
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
    private fun AppUpdateSimulatorSection(simulator: AppUpdateSimulator) {
        if (simulator.isEnabled.not()) return

        val scope = rememberCoroutineScope()

        EditorialSectionHeader(
            eyebrow = "Debug",
            headline = "App update simulator",
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            SoftcoverButton(
                label = "Simulate update available",
                style = ButtonStyle.TONAL,
                size = ButtonSize.S,
                onClick = { scope.launch { simulator.simulateUpdateAvailable() } },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            SoftcoverButton(
                label = "Simulate downloading",
                style = ButtonStyle.TONAL,
                size = ButtonSize.S,
                onClick = { simulator.simulateDownloading() },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            SoftcoverButton(
                label = "Simulate downloaded",
                style = ButtonStyle.TONAL,
                size = ButtonSize.S,
                onClick = { simulator.simulateDownloaded() },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            SoftcoverButton(
                label = "Simulate failure",
                style = ButtonStyle.TONAL,
                size = ButtonSize.S,
                onClick = { simulator.simulateFailed() },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            SoftcoverButton(
                label = "Reset",
                style = ButtonStyle.TEXT,
                size = ButtonSize.S,
                onClick = { scope.launch { simulator.reset() } },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    @Composable
    private fun SettingsGroup(content: @Composable () -> Unit) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }

    @Composable
    private fun SettingsRowDivider() {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }

    @Composable
    internal fun SettingsRow(
        title: String,
        icon: SoftcoverIconResource,
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

            Icon(
                painter = painterResource(R.drawable.ic_keyboard_arrow_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    @Composable
    private fun AppUpdateSection(
        appUpdateState: AppUpdateState,
        onClick: () -> Unit,
    ) {
        val eyebrow = when (appUpdateState) {
            AppUpdateState.Downloading -> "Downloading"
            AppUpdateState.Downloaded -> "Ready to install"
            AppUpdateState.Failed -> "Update failed"
            else -> "Update available"
        }

        val headline = when (appUpdateState) {
            AppUpdateState.Downloading -> "Hold tight, the new build is on its way."
            AppUpdateState.Downloaded -> "Restart Softcover to finish installing."
            AppUpdateState.Failed -> "Something went wrong — tap to try again."
            else -> "A new version of Softcover is ready."
        }

        val isClickable = appUpdateState != AppUpdateState.Downloading

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable { if (isClickable) onClick() },
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 20.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BadgedBox(
                    badge = { Badge() },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_apk_install),
                        contentDescription = "Update icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = eyebrow.uppercase(),
                        style = MaterialTheme.editorialTypography.eyebrowSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = headline,
                        style = MaterialTheme.editorialTypography.body,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (isClickable) {
                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        painter = painterResource(R.drawable.ic_keyboard_arrow_right),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    @Composable
    private fun VersionFooter(
        versionName: String,
        versionCode: Int,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Version $versionName ($versionCode)",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@StandardPreview
@Composable
private fun SettingsScreenPreview() {
    SoftcoverTheme {
        SettingsScreen.Screen(
            runAction = {},
            state = SettingsScreenUiState(),
            navigateToProfile = {},
            navigateToAppearanceSettings = {},
            navigateToLibraryVisibility = {},
        )
    }
}
