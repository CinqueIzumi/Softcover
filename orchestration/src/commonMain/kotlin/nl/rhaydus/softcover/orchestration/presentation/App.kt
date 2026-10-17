package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.distinctUntilChanged
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.layout.WindowWidthClass
import nl.rhaydus.designsystem.layout.rememberWindowSizeClass
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.component.haptics.play
import nl.rhaydus.softcover.core.component.snackbar.SoftcoverSnackbarHost
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.appupdate.ObserveAppUpdateStateUseCase
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.core.presentation.cover.ProvideCoverImagePersister
import nl.rhaydus.softcover.core.presentation.theme.LocalThemeConfiguration
import nl.rhaydus.softcover.core.presentation.util.LocalAppUpdateState
import nl.rhaydus.softcover.core.presentation.util.LocalStartAppUpdate
import nl.rhaydus.softcover.core.uibinding.theme.toSchemeBrightness
import nl.rhaydus.softcover.feature.app_update.domain.usecase.CompleteAppUpdateUseCase
import nl.rhaydus.softcover.feature.app_update.domain.usecase.StartAppUpdateFlowUseCase
import nl.rhaydus.softcover.feature.onboarding.presentation.screen.OnboardingScreen
import nl.rhaydus.softcover.orchestration.presentation.viewmodel.MainActivityViewModel
import org.koin.compose.koinInject

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
    val spinePalette by viewModel.spinePalette.collectAsStateWithLifecycle()
    val reAuthSheet by viewModel.reAuthSheet.collectAsStateWithLifecycle()

    val appUpdateFlowLauncher = rememberAppUpdateFlowLauncher()

    var appUpdateState by remember { mutableStateOf<AppUpdateState>(AppUpdateState.Idle) }

    val onStartAppUpdate: () -> Unit = {
        when (appUpdateState) {
            AppUpdateState.Downloaded -> completeAppUpdateUseCase()
            else -> startAppUpdateFlowUseCase(appUpdateFlowLauncher)
        }
    }

    ApplyPlatformThemeAppearance(themeMode = themeConfig.themeMode)

    SoftcoverTheme(
        palette = spinePalette,
        // `isSystemInDarkTheme()` stays in composition on purpose: "follow the device" is a platform
        // signal that only exists here. Reading it is not mapping — `component-contract.md` R9 names
        // this as its one carve-out.
        brightness = themeConfig.themeMode.toSchemeBrightness(systemInDark = isSystemInDarkTheme()),
        dynamicColor = themeConfig.useDynamicColor,
    ) {
        val haptics = LocalHaptics.current

        LaunchedEffect(Unit) {
            var previousState: AppUpdateState = AppUpdateState.Idle

            observeAppUpdateStateUseCase()
                .distinctUntilChanged()
                .collect { updateState ->
                    val wasDownloading = previousState is AppUpdateState.Downloading

                    previousState = updateState
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
                            if (wasDownloading) haptics.play(Feedback.Failure)

                            SnackBarManager.showSnackbar(title = "Update failed. Please try again later.")
                        }

                        is AppUpdateState.Downloading,
                        AppUpdateState.Idle -> Unit
                    }
                }
        }

        ClearFocusOnTapScreen {
            ProvideCoverImagePersister {
                CompositionLocalProvider(
                    LocalThemeConfiguration provides themeConfig,
                    LocalAppUpdateState provides appUpdateState,
                    LocalStartAppUpdate provides onStartAppUpdate,
                ) {
                    val bottomChromeAnchor = remember { BottomChromeAnchor() }
                    var hostBottomInRoot by remember { mutableFloatStateOf(0f) }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { coordinates ->
                                hostBottomInRoot = coordinates.boundsInRoot().bottom
                            },
                    ) {
                        // Gate the first navigation on resolved auth state. Until the splash check
                        // finishes `authenticated` defaults to false, so building the navigator eagerly
                        // would mount OnboardingScreen and only swap to RootScreen once auth resolves.
                        // Android hides that frame behind the system splash; desktop has no such splash,
                        // so it flashed the login screen.
                        if (state.isLoading) {
                            Surface(modifier = Modifier.fillMaxSize()) {}
                        } else {
                            key(state.authenticated) {
                                CompositionLocalProvider(LocalBottomChromeAnchor provides bottomChromeAnchor) {
                                    Navigator(
                                        screen = if (state.authenticated) RootScreen else OnboardingScreen,
                                        onBackPressed = null,
                                    )
                                }
                            }
                        }

                        // On an expanded (desktop / large) window a bottom-centre toast floats far from
                        // the action; anchor it bottom-end with a constrained width — the desktop
                        // convention — and keep the phone bottom-centre otherwise. While the compact
                        // bottom chrome is on screen the phone host rides above its reported top edge
                        // (that edge already clears the navigation-bar inset); without it, it only
                        // clears the navigation bar.
                        val onWideWindow =
                            rememberWindowSizeClass().widthClass == WindowWidthClass.EXPANDED

                        val snackbarModifier = if (onWideWindow) {
                            Modifier
                                .align(Alignment.BottomEnd)
                                .navigationBarsPadding()
                                .padding(Spacing.md)
                                .widthIn(max = 420.dp)
                        } else {
                            val chromeTop = bottomChromeAnchor.topInRoot

                            if (chromeTop == null) {
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                            } else {
                                val chromeHeightDp = with(LocalDensity.current) {
                                    (hostBottomInRoot - chromeTop).coerceAtLeast(0f).toDp()
                                }

                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = chromeHeightDp + Spacing.xs)
                            }
                        }

                        SoftcoverSnackbarHost(modifier = snackbarModifier)

                        // Show the re-auth overlay only during an authenticated session — if the user
                        // is already on onboarding they can enter a token through the normal flow.
                        reAuthSheet?.takeIf { state.authenticated }?.let { sheet ->
                            ReAuthSheet(
                                model = sheet,
                                onEvent = { event ->
                                    when (event) {
                                        is ReAuthSheetEvent.ApiKeyChanged ->
                                            viewModel.onReAuthApiKeyChanged(event.value)

                                        ReAuthSheetEvent.Submitted -> viewModel.reAuthenticate()

                                        ReAuthSheetEvent.LoggedOut -> viewModel.logOutFromReAuth()

                                        ReAuthSheetEvent.Dismissed -> viewModel.onReAuthDismissed()
                                    }
                                },
                            )
                        }
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
