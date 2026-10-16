package nl.rhaydus.softcover.feature.widget.presentation.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.color.ColorProvider
import androidx.glance.material3.ColorProviders
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.softcoverColorScheme
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette

@Composable
internal fun SoftcoverWidgetTheme(
    themeConfiguration: ThemeConfiguration,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val resolved = remember(themeConfiguration) {
        val selection = themeConfiguration.themeMode.toSchemeSelection()

        val light = widgetColorScheme(
            context = context,
            themeConfiguration = themeConfiguration,
            brightness = selection.day,
        )

        val dark = widgetColorScheme(
            context = context,
            themeConfiguration = themeConfiguration,
            brightness = selection.night,
        )

        ColorProviders(
            light = light,
            dark = dark,
        ) to WidgetExtraColors(
            surfaceContainerHigh = ColorProvider(
                day = light.surfaceContainerHigh,
                night = dark.surfaceContainerHigh,
            ),
            daySurfaceContainerHigh = ColorProvider(
                day = light.surfaceContainerHigh,
                night = Color.Transparent,
            ),
            arrowScrim = ColorProvider(
                day = light.surface.copy(alpha = ARROW_SCRIM_ALPHA),
                night = dark.surface.copy(alpha = ARROW_SCRIM_ALPHA),
            ),
        )
    }

    CompositionLocalProvider(LocalWidgetExtraColors provides resolved.second) {
        GlanceTheme(
            colors = resolved.first,
            content = content,
        )
    }
}

private fun widgetColorScheme(
    context: Context,
    themeConfiguration: ThemeConfiguration,
    brightness: SchemeBrightness,
): ColorScheme {
    val dynamic = themeConfiguration.useDynamicColor &&
        brightness != SchemeBrightness.DIM &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    return when {
        dynamic && brightness.isDark -> dynamicDarkColorScheme(context)
        dynamic -> dynamicLightColorScheme(context)
        else -> softcoverColorScheme(
            brightness = brightness,
            palette = themeConfiguration.colorPalette.toSpinePalette(),
        )
    }
}

private const val ARROW_SCRIM_ALPHA = 0.7f
