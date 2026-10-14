package nl.rhaydus.softcover.orchestration.presentation

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.core.uibinding.theme.toSchemeBrightness

@Composable
internal actual fun ApplyPlatformThemeAppearance(themeMode: ThemeMode) {
    val activity = LocalActivity.current as? ComponentActivity ?: return

    // `enableEdgeToEdge` resolves the bar-glyph contrast once, at call time, so MainActivity's launch
    // call keeps reading the *system's* dark mode for the rest of the session. Re-applying it whenever
    // the resolved brightness flips is the sanctioned correction — the call is idempotent, and under
    // ThemeMode.SYSTEM the brightness recomposes with the device, so this tracks a system flip too.
    val darkTheme = themeMode.toSchemeBrightness(systemInDark = isSystemInDarkTheme()).isDark

    DisposableEffect(
        activity,
        darkTheme,
    ) {
        activity.enableEdgeToEdge(
            statusBarStyle = transparentSystemBarStyle(darkTheme = darkTheme),
            navigationBarStyle = transparentSystemBarStyle(darkTheme = darkTheme),
        )

        onDispose {}
    }
}
