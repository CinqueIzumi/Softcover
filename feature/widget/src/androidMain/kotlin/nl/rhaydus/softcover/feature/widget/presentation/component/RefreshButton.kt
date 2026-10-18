package nl.rhaydus.softcover.feature.widget.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.size
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import nl.rhaydus.softcover.feature.widget.R

/** A 40dp tap target around a refresh icon, shown where a failed fetch can be tried again. */
@Composable
internal fun RefreshButton(onClick: Action) {
    val description = LocalContext.current.getString(R.string.widget_refresh)

    Box(
        modifier = GlanceModifier
            .size(40.dp)
            .semantics { contentDescription = description }
            .clickable(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_refresh),
            contentDescription = null,
            modifier = GlanceModifier.size(20.dp),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
        )
    }
}
