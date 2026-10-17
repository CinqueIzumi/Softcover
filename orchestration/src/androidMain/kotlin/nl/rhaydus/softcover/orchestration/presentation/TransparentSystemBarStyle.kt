package nl.rhaydus.softcover.orchestration.presentation

import androidx.activity.SystemBarStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * A fully transparent system-bar style. [darkTheme] decides the glyph contrast: pass the app's
 * resolved brightness once the theme preference is known, or `null` before it is (at launch), which
 * leaves the decision to the platform's own dark-mode flag — the right answer for the default
 * "follow the system" mode and the only one available before the stored preference has loaded.
 */
internal fun transparentSystemBarStyle(darkTheme: Boolean?): SystemBarStyle {
    val transparent = Color.Transparent.toArgb()

    return if (darkTheme == null) {
        SystemBarStyle.auto(
            lightScrim = transparent,
            darkScrim = transparent,
        )
    } else {
        SystemBarStyle.auto(
            lightScrim = transparent,
            darkScrim = transparent,
            detectDarkMode = { darkTheme },
        )
    }
}
