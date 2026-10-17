package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.domain.model.ThemeMode

/**
 * Desktop has no platform chrome inside the window — the title bar is the OS's own and follows the
 * desktop environment's theme, not the app's — so there is nothing to hand the mode to.
 */
@Composable
internal actual fun ApplyPlatformThemeAppearance(themeMode: ThemeMode) = Unit
