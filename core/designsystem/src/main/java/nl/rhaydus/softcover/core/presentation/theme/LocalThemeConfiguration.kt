package nl.rhaydus.softcover.core.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration

val LocalThemeConfiguration = compositionLocalOf { ThemeConfiguration() }
