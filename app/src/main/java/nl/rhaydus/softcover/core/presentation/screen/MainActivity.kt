package nl.rhaydus.softcover.core.presentation.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import nl.rhaydus.softcover.core.presentation.modifier.noRippleClickable
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.viewmodel.MainActivityViewModel
import nl.rhaydus.softcover.feature.onboarding.presentation.screen.OnboardingScreen
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity() : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModel()

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

            SoftcoverTheme {
                ClearFocusOnTapScreen {
                    key(state.authenticated) {
                        Navigator(
                            screen = if (state.authenticated) RootScreen else OnboardingScreen,
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
        modifier = Modifier
            .fillMaxSize()
            .noRippleClickable { focusManager.clearFocus() }
    ) {
        content()
    }
}