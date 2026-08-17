package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.domain.model.ThemeMode

/**
 * Tells the platform's own chrome — the chrome Compose does not paint — which way the app is now
 * leaning, so a forced [ThemeMode.LIGHT] or [ThemeMode.DARK] doesn't leave the system bars reading
 * the device's setting instead of the reader's. Without it, forcing Light on a dark phone paints
 * white status-bar glyphs onto Softcover's light surface.
 *
 * [ThemeMode.SYSTEM] hands the decision back to the platform rather than pinning it to whatever the
 * device happens to be showing right now, so a device that flips mid-session is still followed.
 *
 * Android re-applies its edge-to-edge bar styles; iOS overrides the key window's user-interface
 * style; desktop has no such chrome and does nothing.
 */
@Composable
internal expect fun ApplyPlatformThemeAppearance(themeMode: ThemeMode)
