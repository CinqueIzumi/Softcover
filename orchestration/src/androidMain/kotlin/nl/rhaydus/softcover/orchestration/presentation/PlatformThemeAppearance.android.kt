package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.designsystem.presentation.systembar.ApplySystemBarAppearance
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.core.uibinding.theme.toSchemeBrightness

@Composable
internal actual fun ApplyPlatformThemeAppearance(themeMode: ThemeMode) {
    ApplySystemBarAppearance(
        isDark = themeMode.toSchemeBrightness(systemInDark = isSystemInDarkTheme()).isDark,
    )
}
