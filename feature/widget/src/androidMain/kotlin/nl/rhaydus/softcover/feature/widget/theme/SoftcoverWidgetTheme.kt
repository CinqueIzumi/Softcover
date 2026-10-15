package nl.rhaydus.softcover.feature.widget.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.material3.ColorProviders
import nl.rhaydus.softcover.core.designsystem.presentation.theme.softcoverColorScheme
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette

@Composable
internal fun SoftcoverWidgetTheme(
    themeConfiguration: ThemeConfiguration,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val colors = remember(themeConfiguration) {
        val selection = themeConfiguration.themeMode.toSchemeSelection()

        ColorProviders(
            light = widgetColorScheme(
                context = context,
                themeConfiguration = themeConfiguration,
                darkTheme = selection.dayIsDark,
            ),
            dark = widgetColorScheme(
                context = context,
                themeConfiguration = themeConfiguration,
                darkTheme = selection.nightIsDark,
            ),
        )
    }

    GlanceTheme(
        colors = colors,
        content = content,
    )
}

private fun widgetColorScheme(
    context: Context,
    themeConfiguration: ThemeConfiguration,
    darkTheme: Boolean,
): ColorScheme {
    val dynamic = themeConfiguration.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    return when {
        dynamic && darkTheme -> dynamicDarkColorScheme(context)
        dynamic -> dynamicLightColorScheme(context)
        else -> softcoverColorScheme(
            darkTheme = darkTheme,
            palette = themeConfiguration.colorPalette.toSpinePalette(),
        )
    }
}
