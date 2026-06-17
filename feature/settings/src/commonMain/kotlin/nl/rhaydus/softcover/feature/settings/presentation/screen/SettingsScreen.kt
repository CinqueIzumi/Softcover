package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.debug.DebugRoutesContent
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.designsystem.presentation.util.LocalAppUpdateState
import nl.rhaydus.softcover.core.designsystem.presentation.util.LocalStartAppUpdate
import nl.rhaydus.softcover.core.domain.appupdate.AppUpdateSimulator
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<SettingsScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val navigator = LocalNavigator.currentOrThrow
        val appNavigator = koinInject<AppNavigator>()
        val debugRoutesContent = koinInject<DebugRoutesContent>()
        val appUpdateSimulator = koinInject<AppUpdateSimulator>()
        val appUpdateState = LocalAppUpdateState.current
        val onStartAppUpdate = LocalStartAppUpdate.current

        // Desktop hosts the Library-tabs settings inline in the master–detail pane, so it needs that
        // screen's model under the Settings lifecycle. Mobile pushes a separate screen (with its own
        // model) instead, so it skips this to avoid spinning up the collectors twice. `koinScreenModel`
        // is a `Screen` extension, so the model must be obtained here in `Content`, not in the layout.
        val libraryVisibilityModel = if (settingsUsesMasterDetail) {
            koinScreenModel<LibraryVisibilitySettingsScreenModel>()
        } else {
            null
        }

        val libraryVisibilityState = if (libraryVisibilityModel != null) {
            libraryVisibilityModel.state.collectAsStateWithLifecycle().value
        } else {
            LibraryVisibilitySettingsUiState()
        }

        SettingsScreenLayout(
            state = state,
            settingsRunAction = screenModel::runAction,
            navigateToProfile = {
                navigator.parent?.push(appNavigator.screen(ScreenDestination.Profile))
            },
            navigateToAppearanceSettings = {
                navigator.parent?.push(AppearanceSettingsScreen())
            },
            navigateToLibraryVisibility = {
                navigator.parent?.push(LibraryVisibilitySettingsScreen())
            },
            libraryVisibilityState = libraryVisibilityState,
            libraryVisibilityRunAction = { action -> libraryVisibilityModel?.runAction(action) },
            onCreateListClick = {
                navigator.parent?.push(appNavigator.screen(ScreenDestination.CreateList))
            },
            appUpdateState = appUpdateState,
            onStartAppUpdate = onStartAppUpdate,
            debugSection = {
                debugRoutesContent.Render()

                AppUpdateSimulatorSection(simulator = appUpdateSimulator)
            },
        )
    }
}

/**
 * Whether the desktop master–detail Settings surface is in use (`true` on `jvmMain`, `false` on
 * `mobileMain`). It gates the eager creation of the Library-tabs screen model in [SettingsScreen]:
 * only the desktop layout hosts that pane inline. Mirrors the `wideGridTabsUseDetailPane` seam.
 */
internal expect val settingsUsesMasterDetail: Boolean

/**
 * The Settings screen body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke
 * `actual`: the shared `ScreenModel` / state / actions wire up identically in [SettingsScreen.Content],
 * and only the rendered layout branches — mobile keeps the scrolling editorial menu that pushes its
 * sub-pages, desktop renders a category-sidebar master–detail. `expect` cannot carry default argument
 * values, so every parameter is supplied explicitly at the single call site above.
 */
@Composable
internal expect fun SettingsScreenLayout(
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
)

/**
 * Debug-only build switches for simulating the in-app update flow. Lives in `commonMain` because it is
 * raised by [SettingsScreen.Content] as the `debugSection` slot, which both platform layouts render.
 */
@Composable
internal fun AppUpdateSimulatorSection(simulator: AppUpdateSimulator) {
    if (simulator.isEnabled.not()) return

    val scope = rememberCoroutineScope()

    EditorialSectionHeader(
        eyebrow = "Debug",
        headline = "App update simulator",
    )

    Spacer(modifier = Modifier.height(16.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
        RhaydusButton(
            label = "Simulate update available",
            style = ButtonStyle.TONAL,
            size = ButtonSize.S,
            onClick = { scope.launch { simulator.simulateUpdateAvailable() } },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        RhaydusButton(
            label = "Simulate downloading",
            style = ButtonStyle.TONAL,
            size = ButtonSize.S,
            onClick = { simulator.simulateDownloading() },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        RhaydusButton(
            label = "Simulate downloaded",
            style = ButtonStyle.TONAL,
            size = ButtonSize.S,
            onClick = { simulator.simulateDownloaded() },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        RhaydusButton(
            label = "Simulate failure",
            style = ButtonStyle.TONAL,
            size = ButtonSize.S,
            onClick = { simulator.simulateFailed() },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        RhaydusButton(
            label = "Reset",
            style = ButtonStyle.TEXT,
            size = ButtonSize.S,
            onClick = { scope.launch { simulator.reset() } },
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Spacer(modifier = Modifier.height(40.dp))
}
