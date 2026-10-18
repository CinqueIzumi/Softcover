package nl.rhaydus.softcover.feature.widget.presentation.component

import androidx.compose.runtime.Composable
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import nl.rhaydus.softcover.feature.widget.R

/**
 * The grey stand-in for a cover that is pending or missing. It fills the cover slot and fits the 2:3 vector inside
 * it, so it is the size the real cover is.
 */
@Composable
internal fun CoverPlaceholder() {
    Image(
        provider = ImageProvider(R.drawable.widget_cover_placeholder),
        contentDescription = null,
        modifier = GlanceModifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(GlanceTheme.colors.surfaceVariant),
    )
}
