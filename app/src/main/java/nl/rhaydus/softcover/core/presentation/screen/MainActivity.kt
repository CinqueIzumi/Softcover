package nl.rhaydus.softcover.core.presentation.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.presentation.modifier.noRippleClickable
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.core.presentation.viewmodel.MainActivityViewModel
import nl.rhaydus.softcover.feature.app_update.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.app_update.domain.usecase.CheckForAppUpdateUseCase
import nl.rhaydus.softcover.feature.app_update.domain.usecase.CompleteAppUpdateUseCase
import nl.rhaydus.softcover.feature.app_update.domain.usecase.ObserveAppUpdateStateUseCase
import nl.rhaydus.softcover.feature.app_update.domain.usecase.StartAppUpdateFlowUseCase
import nl.rhaydus.softcover.feature.onboarding.presentation.screen.OnboardingScreen
import nl.rhaydus.softcover.feature.settings.domain.model.ThemeConfiguration
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

val LocalThemeConfiguration = compositionLocalOf { ThemeConfiguration() }

class MainActivity() : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModel()
    private val observeAppUpdateStateUseCase: ObserveAppUpdateStateUseCase by inject()
    private val checkForAppUpdateUseCase: CheckForAppUpdateUseCase by inject()
    private val startAppUpdateFlowUseCase: StartAppUpdateFlowUseCase by inject()
    private val completeAppUpdateUseCase: CompleteAppUpdateUseCase by inject()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transparent = Color.Transparent.toArgb()
        val transparentAutoStyle = SystemBarStyle.auto(
            lightScrim = transparent,
            darkScrim = transparent,
        )

        enableEdgeToEdge(
            statusBarStyle = transparentAutoStyle,
            navigationBarStyle = transparentAutoStyle,
        )

        installSplashScreen().setKeepOnScreenCondition {
            viewModel.state.value.isLoading
        }

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val themeConfig by viewModel.themeState.collectAsStateWithLifecycle()
            val snackBarState by SnackBarManager.snackBarState.collectAsStateWithLifecycle()

            val updateLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
            ) { }

            var appUpdateState by remember { mutableStateOf<AppUpdateState>(AppUpdateState.Idle) }

            val onStartAppUpdate: () -> Unit = {
                when (appUpdateState) {
                    AppUpdateState.Downloaded -> completeAppUpdateUseCase()
                    else -> startAppUpdateFlowUseCase(launcher = updateLauncher)
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
                                        startAppUpdateFlowUseCase(launcher = updateLauncher)
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
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            checkForAppUpdateUseCase()
        }
    }
}

@Composable
private fun ClearFocusOnTapScreen(content: @Composable () -> Unit) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .noRippleClickable { focusManager.clearFocus() }
    ) {
        content()
    }
}
