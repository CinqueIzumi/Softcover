package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.runtime.compositionLocalOf

/**
 * Whether the surrounding [SoftcoverTheme] is painting dark. This is the app's answer, not the
 * device's: with a forced Light or Dark [nl.rhaydus.softcover.core.domain.model.ThemeMode] the two
 * part ways, so a composable deciding an alpha or an ink by brightness must read this rather than
 * `isSystemInDarkTheme()`.
 */
val LocalDarkTheme = compositionLocalOf { false }
