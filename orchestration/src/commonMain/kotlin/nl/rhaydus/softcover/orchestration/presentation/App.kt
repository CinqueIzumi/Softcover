package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.koinInject
import nl.rhaydus.designsystem.layout.WindowWidthClass
import nl.rhaydus.designsystem.layout.rememberWindowSizeClass
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalThemeConfiguration
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.util.LocalAppUpdateState
import nl.rhaydus.softcover.core.designsystem.presentation.util.LocalStartAppUpdate
import nl.rhaydus.softcover.core.designsystem.presentation.viewmodel.MainActivityViewModel
import nl.rhaydus.softcover.core.domain.message.UserMessageNotifier
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.app_update.domain.usecase.CompleteAppUpdateUseCase
import nl.rhaydus.softcover.feature.app_update.domain.usecase.ObserveAppUpdateStateUseCase
import nl.rhaydus.softcover.feature.app_update.domain.usecase.StartAppUpdateFlowUseCase
import nl.rhaydus.softcover.feature.onboarding.presentation.screen.OnboardingScreen

/** Cap a snackbar's width on a wide window so it doesn't stretch across the whole desktop frame. */
private val SNACKBAR_DESKTOP_MAX_WIDTH = 420.dp

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
    val reAuthState by viewModel.reAuthState.collectAsStateWithLifecycle()
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

                    // On an expanded (desktop / large) window a bottom-centre toast floats far from
                    // the action; anchor it bottom-end with a constrained width — the desktop
                    // convention — and keep the phone bottom-centre otherwise.
                    val onWideWindow =
                        rememberWindowSizeClass().widthClass == WindowWidthClass.EXPANDED

                    val snackbarModifier = if (onWideWindow) {
                        Modifier
                            .align(Alignment.BottomEnd)
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .widthIn(max = SNACKBAR_DESKTOP_MAX_WIDTH)
                    } else {
                        Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                    }

                    SnackbarHost(
                        hostState = snackBarState,
                        modifier = snackbarModifier,
                    )

                    // Show the re-auth overlay only during an authenticated session — if the user
                    // is already on onboarding they can enter a token through the normal flow.
                    if (reAuthState.visible && state.authenticated) {
                        ReAuthDialog(
                            isSubmitting = reAuthState.isSubmitting,
                            errorMessage = reAuthState.errorMessage,
                            onSubmit = viewModel::reAuthenticate,
                            onLogOut = viewModel::logOutFromReAuth,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClearFocusOnTapScreen(content: @Composable () -> Unit) {
    val focusManager = LocalFocusManager.current

    Box(
        // A pointer-tap dismiss scrim — deliberately NOT a `clickable`. On desktop `clickable` also
        // binds Spacebar/Enter as activation keys, so a focused text field that doesn't fully consume
        // the space key bubbles it up here and clears its own focus mid-typing. `detectTapGestures`
        // reacts to pointer taps only (no key binding, not focusable), preserving tap-to-dismiss on
        // both touch and desktop without stealing focus on space.
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
    ) {
        content()
    }
}
