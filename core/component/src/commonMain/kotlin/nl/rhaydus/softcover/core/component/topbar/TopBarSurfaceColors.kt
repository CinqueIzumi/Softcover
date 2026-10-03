package nl.rhaydus.softcover.core.component.topbar

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

internal data class TopBarSurfaceColors(
    val container: Color,
    val controlContainer: Color,
    val controlContent: Color,
)

private val OVER_MEDIA_SCRIM = Color.Black.copy(alpha = 0.35f)

internal fun TopBarSurface.colors(scheme: ColorScheme): TopBarSurfaceColors = when (this) {
    TopBarSurface.OPAQUE -> TopBarSurfaceColors(
        container = scheme.surface,
        controlContainer = Color.Transparent,
        controlContent = scheme.onSurface,
    )

    TopBarSurface.OVER_MEDIA -> TopBarSurfaceColors(
        container = Color.Transparent,
        controlContainer = OVER_MEDIA_SCRIM,
        controlContent = Color.White,
    )
}
