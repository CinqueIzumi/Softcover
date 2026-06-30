package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

// iOS has no Material You / dynamic-color equivalent — always fall back to the brand scheme.
@Composable
actual fun dynamicColorSchemeOrNull(
    useDynamicColor: Boolean,
    darkTheme: Boolean,
): ColorScheme? = null
