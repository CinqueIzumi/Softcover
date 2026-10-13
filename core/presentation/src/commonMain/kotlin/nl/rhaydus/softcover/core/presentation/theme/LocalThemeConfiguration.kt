package nl.rhaydus.softcover.core.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration

/**
 * The reader's whole appearance preference, provided once by `App` so any surface can read a part of
 * it without re-collecting the settings flow — the bottom-bar style, the chosen brightness, the spine
 * colour.
 *
 * It lives here rather than in `:core:designsystem` because a [ThemeConfiguration] is *persisted app
 * state*, not a design token: the design system takes a resolved `SpinePalette` and a resolved
 * brightness, and knows nothing about how either was chosen or stored.
 */
val LocalThemeConfiguration = compositionLocalOf { ThemeConfiguration() }
