package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.runtime.compositionLocalOf

/**
 * The brightness the surrounding [SoftcoverTheme] is painting. This is the app's answer, not the
 * device's: when the reader has forced a look the two part ways, so a composable deciding an alpha
 * or an ink by brightness must read this (via [SchemeBrightness.isDark]) rather than
 * `isSystemInDarkTheme()`.
 */
val LocalSchemeBrightness = compositionLocalOf { SchemeBrightness.LIGHT }
