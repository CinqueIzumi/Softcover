package nl.rhaydus.softcover.feature.widget.presentation.component

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.appwidget.ImageProvider
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.unit.ColorProvider
import nl.rhaydus.softcover.feature.widget.presentation.theme.LocalWidgetExtraColors

/** [backdrop] is `null` while the book has no cover, which swaps the artwork for the two colour washes. */
@Composable
internal fun BackdropUris(
    backdrop: Uri?,
    veil: Uri,
    veilLift: Uri,
    washPrimary: Uri,
    washTertiary: Uri,
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
        UriMask(
            mask = washPrimary,
            tint = GlanceTheme.colors.primary,
        )

        UriMask(
            mask = washTertiary,
            tint = GlanceTheme.colors.tertiary,
        )
    }

    UriMask(
        mask = veil,
        tint = extra.surfaceContainerHigh,
    )

    UriMask(
        mask = veilLift,
        tint = extra.daySurfaceContainerHigh,
    )
}

@Composable
internal fun UriMask(
    mask: Uri,
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
