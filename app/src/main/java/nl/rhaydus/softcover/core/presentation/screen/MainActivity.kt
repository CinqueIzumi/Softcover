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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import cafe.adriel.voyager.navigator.Navigator
import dagger.hilt.android.AndroidEntryPoint
import nl.rhaydus.softcover.core.presentation.modifier.noRippleClickable
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transparent = Color.Companion.Transparent.toArgb()
        val transparentAutoStyle = SystemBarStyle.Companion.auto(
            lightScrim = transparent,
            darkScrim = transparent,
        )

        enableEdgeToEdge(
            statusBarStyle = transparentAutoStyle,
            navigationBarStyle = transparentAutoStyle,
        )

        setContent {
            SoftcoverTheme {
                ClearFocusOnTapScreen {
                    Navigator(RootScreen)
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