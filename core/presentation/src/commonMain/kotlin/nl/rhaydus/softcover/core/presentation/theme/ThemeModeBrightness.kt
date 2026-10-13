package nl.rhaydus.softcover.core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.domain.model.ThemeMode

/**
 * Whether this mode paints dark right now. [ThemeMode.SYSTEM] defers to the platform's own setting
 * and therefore re-resolves whenever the device flips; the two explicit modes ignore it.
 *
 * Read this — or `LocalDarkTheme` once inside the theme — rather than `isSystemInDarkTheme()`: with a
 * forced Light or Dark mode the system's answer is no longer the app's.
 *
 * This is the seam between the reader's stored preference and the design system's `SoftcoverTheme`,
 * which takes an already-resolved `darkTheme: Boolean`. Resolving `SYSTEM` needs the preference, so
 * it belongs on this side of that seam.
 */
@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
}
