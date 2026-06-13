package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.noRippleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalThemeConfiguration
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.util.LocalAppUpdateState
import nl.rhaydus.softcover.core.designsystem.presentation.util.LocalStartAppUpdate
import nl.rhaydus.softcover.core.designsystem.presentation.util.SnackBarManager
import nl.rhaydus.softcover.core.designsystem.presentation.viewmodel.MainActivityViewModel
import nl.rhaydus.softcover.core.domain.message.UserMessageNotifier
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.app_update.domain.usecase.CompleteAppUpdateUseCase
import nl.rhaydus.softcover.feature.app_update.domain.usecase.ObserveAppUpdateStateUseCase
import nl.rhaydus.softcover.feature.app_update.domain.usecase.StartAppUpdateFlowUseCase
import nl.rhaydus.softcover.feature.onboarding.presentation.screen.OnboardingScreen

/**
 * The shared application root. Holds the theme, the authenticated/onboarding navigator swap, the
 * snackbar host, and the app-update + user-message wiring — everything platform-neutral. Each
 * platform's entry point (Android's `MainActivity`, a future iOS `MainViewController`) provides only
 * its own chrome and hosts this composable.
 */
@Composable
internal fun App() {
    val viewModel = koinInject<MainActivityViewModel>()
    val observeAppUpdateStateUseCase = koinInject<ObserveAppUpdateStateUseCase>()
    val startAppUpdateFlowUseCase = koinInject<StartAppUpdateFlowUseCase>()
    val completeAppUpdateUseCase = koinInject<CompleteAppUpdateUseCase>()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val themeConfig by viewModel.themeState.collectAsStateWithLifecycle()
    val snackBarState by SnackBarManager.snackBarState.collectAsStateWithLifecycle()

    val appUpdateFlowLauncher = rememberAppUpdateFlowLauncher()

    var appUpdateState by remember { mutableStateOf<AppUpdateState>(AppUpdateState.Idle) }

    val onStartAppUpdate: () -> Unit = {
        when (appUpdateState) {
            AppUpdateState.Downloaded -> completeAppUpdateUseCase()
            else -> startAppUpdateFlowUseCase(appUpdateFlowLauncher)
        }
    }

    LaunchedEffect(Unit) {
        observeAppUpdateStateUseCase()
            .distinctUntilChanged()
            .collect { updateState ->
                appUpdateState = updateState

                when (updateState) {
                    AppUpdateState.Available -> {
                        SnackBarManager.showSnackBar(
                            title = "A new version of Softcover is available.",
                            actionLabel = "Update",
                            duration = SnackbarDuration.Indefinite,
                            onActionClick = {
                                startAppUpdateFlowUseCase(appUpdateFlowLauncher)
                            },
                        )
                    }

                    AppUpdateState.Downloaded -> {
                        SnackBarManager.showSnackBar(
                            title = "Update downloaded. Restart to finish installing.",
                            actionLabel = "Restart",
                            duration = SnackbarDuration.Indefinite,
                            onActionClick = {
                                completeAppUpdateUseCase()
                            },
                        )
                    }

                    AppUpdateState.Failed -> {
                        SnackBarManager.showSnackbar(title = "Update failed. Please try again later.")
                    }

                    AppUpdateState.Downloading,
                    AppUpdateState.Idle -> Unit
                }
            }
    }

    LaunchedEffect(Unit) {
        UserMessageNotifier.messages.collect { message ->
            SnackBarManager.showSnackbar(title = message)
        }
    }

    SoftcoverTheme(dynamicColor = themeConfig.useDynamicColor) {
        ClearFocusOnTapScreen {
            CompositionLocalProvider(
                LocalThemeConfiguration provides themeConfig,
                LocalAppUpdateState provides appUpdateState,
                LocalStartAppUpdate provides onStartAppUpdate,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    key(state.authenticated) {
                        Navigator(
                            screen = if (state.authenticated) RootScreen else OnboardingScreen,
                        )
                    }

                    SnackbarHost(
                        hostState = snackBarState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ClearFocusOnTapScreen(content: @Composable () -> Unit) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .noRippleClickable { focusManager.clearFocus() },
    ) {
        content()
    }
}
