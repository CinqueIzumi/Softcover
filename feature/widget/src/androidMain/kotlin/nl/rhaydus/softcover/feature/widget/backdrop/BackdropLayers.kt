package nl.rhaydus.softcover.feature.widget.backdrop

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.unit.ColorProvider
import nl.rhaydus.softcover.feature.widget.theme.LocalWidgetExtraColors

@Composable
internal fun Backdrop(
    backdrop: Bitmap?,
    artwork: BackdropArtwork,
) {
    val extra = LocalWidgetExtraColors.current

    if (backdrop != null) {
        Image(
            provider = ImageProvider(backdrop),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    } else {
        Mask(
            mask = artwork.gradients.washPrimary,
            tint = GlanceTheme.colors.primary,
        )

        Mask(
            mask = artwork.gradients.washTertiary,
            tint = GlanceTheme.colors.tertiary,
        )
    }

    Mask(
        mask = artwork.gradients.veil,
        tint = extra.surfaceContainerHigh,
    )

    Mask(
        mask = artwork.gradients.veilLift,
        tint = extra.daySurfaceContainerHigh,
    )
}

@Composable
internal fun Mask(
    mask: Bitmap,
    tint: ColorProvider,
    contentScale: ContentScale = ContentScale.FillBounds,
) {
    Image(
        provider = ImageProvider(mask),
        contentDescription = null,
        modifier = GlanceModifier.fillMaxSize(),
        contentScale = contentScale,
        colorFilter = ColorFilter.tint(tint),
    )
}
