package nl.rhaydus.softcover.showcase.screen

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import nl.rhaydus.softcover.core.component.snackbar.SoftcoverSnackbarHost
import nl.rhaydus.softcover.core.designsystem.presentation.systembar.ApplySystemBarAppearance
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.showcase.home.screen.HomeScreen
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import org.koin.compose.koinInject

/**
 * The showcase's root: the chosen look wraps the navigator and the snackbar host, so every screen,
 * sheet and dialog opened below it takes the same palette and brightness.
 */
@Composable
internal fun ShowcaseRoot() {
    val themeStore = koinInject<ShowcaseThemeStore>()

    val theme by themeStore.theme.collectAsStateWithLifecycle()

    ApplySystemBarAppearance(isDark = theme.brightness.isDark)

    val activity = LocalActivity.current

    DisposableEffect(
        activity,
        theme.brightness.isDark,
    ) {
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isNavigationBarContrastEnforced = false
        }

        onDispose {}
    }

    SoftcoverTheme(
        palette = theme.palette,
        brightness = theme.brightness,
        dynamicColor = false,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Navigator(screen = HomeScreen())

                SoftcoverSnackbarHost(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                )
            }
        }
    }
}
