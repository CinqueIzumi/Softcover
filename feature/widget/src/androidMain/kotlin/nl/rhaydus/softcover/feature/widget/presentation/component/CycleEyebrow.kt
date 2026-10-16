package nl.rhaydus.softcover.feature.widget.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import nl.rhaydus.softcover.feature.widget.R

/** A 28dp label row with a 40dp-wide ‹ at its start and › at its end; a `null` action leaves that arrow out. */
@Composable
internal fun CycleEyebrow(
    label: String,
    previous: Action?,
    next: Action?,
    previousDescription: String,
    nextDescription: String,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (previous != null) {
            CycleArrow(
                icon = R.drawable.ic_widget_chevron_left,
                action = previous,
                description = previousDescription,
            )
        }

        Box(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                maxLines = 1,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            )
        }

        if (next != null) {
            CycleArrow(
                icon = R.drawable.ic_widget_chevron_right,
                action = next,
                description = nextDescription,
            )
        }
    }
}

@Composable
private fun CycleArrow(
    @DrawableRes icon: Int,
    action: Action,
    description: String,
) {
    Box(
        modifier = GlanceModifier
            .width(40.dp)
            .fillMaxHeight()
            .semantics { contentDescription = description }
            .clickable(action),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(icon),
            contentDescription = null,
            modifier = GlanceModifier.size(20.dp),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
        )
    }
}
