package nl.rhaydus.softcover.feature.widget.presentation.component

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
import nl.rhaydus.softcover.feature.widget.presentation.theme.LocalWidgetExtraColors

/** [backdrop] is `null` while the book has no cover, which swaps the artwork for the two colour washes. */
@Composable
internal fun BackdropLayers(
    backdrop: Bitmap?,
    veil: Bitmap,
    veilLift: Bitmap,
    washPrimary: Bitmap,
    washTertiary: Bitmap,
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
        BitmapMask(
            mask = washPrimary,
            tint = GlanceTheme.colors.primary,
        )

        BitmapMask(
            mask = washTertiary,
            tint = GlanceTheme.colors.tertiary,
        )
    }

    BitmapMask(
        mask = veil,
        tint = extra.surfaceContainerHigh,
    )

    BitmapMask(
        mask = veilLift,
        tint = extra.daySurfaceContainerHigh,
    )
}

@Composable
internal fun BitmapMask(
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
