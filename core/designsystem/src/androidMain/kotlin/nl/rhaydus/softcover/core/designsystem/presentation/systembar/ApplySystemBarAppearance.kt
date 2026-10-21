package nl.rhaydus.softcover.core.designsystem.presentation.systembar

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
fun ApplySystemBarAppearance(isDark: Boolean) {
    val activity = LocalActivity.current as? ComponentActivity ?: return

    // `enableEdgeToEdge` resolves the bar-glyph contrast once, at call time, so MainActivity's launch
    // call keeps reading the *system's* dark mode for the rest of the session. Re-applying it whenever
    // the resolved brightness flips is the sanctioned correction — the call is idempotent, and under
    // ThemeMode.SYSTEM the brightness recomposes with the device, so this tracks a system flip too.
    DisposableEffect(
        activity,
        isDark,
    ) {
        activity.enableEdgeToEdge(
            statusBarStyle = transparentSystemBarStyle(darkTheme = isDark),
            navigationBarStyle = transparentSystemBarStyle(darkTheme = isDark),
        )

        onDispose {}
    }
}
