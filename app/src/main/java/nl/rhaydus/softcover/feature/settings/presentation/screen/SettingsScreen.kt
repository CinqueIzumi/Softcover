package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.BuildConfig
import nl.rhaydus.softcover.R
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.presentation.modifier.noRippleClickable
import nl.rhaydus.softcover.core.presentation.screen.LocalAppUpdateState
import nl.rhaydus.softcover.core.presentation.screen.LocalStartAppUpdate
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.feature.app_update.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.app_update.domain.simulator.AppUpdateSimulator
import nl.rhaydus.softcover.feature.profile.presentation.screen.ProfileScreen
import nl.rhaydus.softcover.feature.search.presentation.screen.SearchScreen
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<SettingsScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val navigator = LocalNavigator.currentOrThrow
        val appUpdateState = LocalAppUpdateState.current
        val onStartAppUpdate = LocalStartAppUpdate.current

        Screen(
            state = state,
            runAction = screenModel::runAction,
            navigateToProfile = {
                navigator.parent?.push(ProfileScreen())
            },
            navigateToAppearanceSettings = {
                navigator.parent?.push(AppearanceSettingsScreen())
            },
            onNavigateToSearch = {
                navigator.parent?.push(item = SearchScreen())
            },
            appUpdateState = appUpdateState,
            onStartAppUpdate = onStartAppUpdate,
            debugSection = { AppUpdateSimulatorSection(simulator = koinInject()) },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        state: SettingsScreenUiState,
        runAction: (SettingsAction) -> Unit,
        navigateToProfile: () -> Unit,
        navigateToAppearanceSettings: () -> Unit,
        onNavigateToSearch: () -> Unit,
        appUpdateState: AppUpdateState = AppUpdateState.Idle,
        onStartAppUpdate: () -> Unit = {},
        debugSection: @Composable () -> Unit = {},
    ) {
        Scaffold(
            topBar = {
                SoftcoverTopBar(
                    title = "Settings",
                    onNavigateToSearch = onNavigateToSearch,
                )
            },
            contentWindowInsets = WindowInsets.statusBars,
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher_foreground),
                        contentDescription = "",
                        modifier = Modifier.size(200.dp),
                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.secondary)
                    )
                }

                HorizontalDivider()

                if (appUpdateState != AppUpdateState.Idle) {
                    AppUpdateSectionCard(
                        appUpdateState = appUpdateState,
                        onClick = onStartAppUpdate,
                    )

                    HorizontalDivider()
                }

                SectionCard(
                    title = "View user profile",
                    onClick = navigateToProfile,
                    icon = SoftcoverIconResource.Drawable(
                        id = R.drawable.ic_account,
                        contentDescription = "Account icon"
                    )
                )

                HorizontalDivider()

                SectionCard(
                    title = "Appearance",
                    onClick = navigateToAppearanceSettings,
                    icon = SoftcoverIconResource.Drawable(
                        id = R.drawable.ic_palette,
                        contentDescription = "Appearance icon"
                    )
                )

                HorizontalDivider()

                debugSection()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    @Composable
    private fun AppUpdateSimulatorSection(simulator: AppUpdateSimulator) {
        if (!simulator.isEnabled) return

        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "App update simulator (debug)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = { scope.launch { simulator.simulateUpdateAvailable() } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Simulate update available")
            }

            FilledTonalButton(
                onClick = { simulator.simulateDownloading() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Simulate downloading")
            }

            FilledTonalButton(
                onClick = { simulator.simulateDownloaded() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Simulate downloaded")
            }

            FilledTonalButton(
                onClick = { simulator.simulateFailed() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Simulate failure")
            }

            FilledTonalButton(
                onClick = { scope.launch { simulator.reset() } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Reset")
            }
        }

        HorizontalDivider()
    }

    @Composable
    internal fun SectionCard(
        title: String,
        icon: SoftcoverIconResource,
        onClick: () -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .noRippleClickable(onClick),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = icon.getIconPainter(),
                    contentDescription = icon.contentDescription,
                    modifier = Modifier.size(20.dp),
                )

                Spacer(modifier = Modifier.width(24.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_keyboard_arrow_right),
                contentDescription = null
            )
        }
    }

    @Composable
    private fun AppUpdateSectionCard(
        appUpdateState: AppUpdateState,
        onClick: () -> Unit,
    ) {
        val title = when (appUpdateState) {
            AppUpdateState.Downloading -> "Update downloading…"
            AppUpdateState.Downloaded -> "Restart to install update"
            AppUpdateState.Failed -> "Update failed — try again"
            else -> "Update available"
        }

        val isClickable = appUpdateState != AppUpdateState.Downloading

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .noRippleClickable { if (isClickable) onClick() },
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgedBox(
                    badge = { Badge() },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_apk_install),
                        contentDescription = "Update icon",
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (isClickable) {
                Icon(
                    painter = painterResource(R.drawable.ic_keyboard_arrow_right),
                    contentDescription = null
                )
            }
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
            onNavigateToSearch = {},
        )
    }
}