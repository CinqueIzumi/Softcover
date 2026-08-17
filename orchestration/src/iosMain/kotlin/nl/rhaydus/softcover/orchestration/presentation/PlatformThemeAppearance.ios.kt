package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle
import nl.rhaydus.softcover.core.domain.model.ThemeMode

@Composable
internal actual fun ApplyPlatformThemeAppearance(themeMode: ThemeMode) {
    // The status bar's glyph colour comes from the window's UIKit trait, not from anything Compose
    // paints, so forcing Light on a dark phone would otherwise leave white glyphs on Softcover's light
    // surface. Overriding the window's interface style moves the trait — and with it the status bar —
    // in step with the reader's choice; SYSTEM clears the override so iOS keeps deciding.
    DisposableEffect(themeMode) {
        val overrideStyle: UIUserInterfaceStyle = when (themeMode) {
            ThemeMode.LIGHT -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
            ThemeMode.DARK -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
            ThemeMode.SYSTEM -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
        }

        // `keyWindow` is soft-deprecated in favour of the scene APIs, which Kotlin/Native can only
        // reach through an untyped `connectedScenes` walk; the single-window Softcover app gets the
        // same window either way, so the simpler call stands.
        @Suppress("DEPRECATION")
        UIApplication.sharedApplication.keyWindow?.setOverrideUserInterfaceStyle(overrideStyle)

        onDispose {}
    }
}
