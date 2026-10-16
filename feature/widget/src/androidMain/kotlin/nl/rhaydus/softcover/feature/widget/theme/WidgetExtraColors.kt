package nl.rhaydus.softcover.feature.widget.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.glance.unit.ColorProvider

/**
 * Roles Glance's `ColorProviders` does not carry, built from the same day and night schemes.
 * [daySurfaceContainerHigh] is transparent at night, for the extra veil lift only light surfaces get.
 */
internal class WidgetExtraColors(
    val surfaceContainerHigh: ColorProvider,
    val daySurfaceContainerHigh: ColorProvider,
    val arrowScrim: ColorProvider,
)

internal val LocalWidgetExtraColors = staticCompositionLocalOf<WidgetExtraColors> {
    error("SoftcoverWidgetTheme provides the extra widget colors")
}
