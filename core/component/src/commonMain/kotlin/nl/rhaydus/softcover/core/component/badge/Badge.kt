package nl.rhaydus.softcover.core.component.badge

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A small pill of chrome reporting a status in one word or a short phrase — a reading deadline's
 * pace, or an edition's release date. Inert: a badge is read, never tapped, so it takes no event
 * lambda (§ 7.2 R1 — there is nothing to hoist when there is nothing to click).
 */
@Composable
fun Badge(
    model: BadgeUiModel,
    modifier: Modifier = Modifier,
) {
    val (container, content) = when (model.tone) {
        BadgeTone.OnTrack -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        BadgeTone.Behind -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        BadgeTone.Expired -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        BadgeTone.Release -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    }

    val dimensions = BadgeDimensions.forVariant(variant = model.variant)

    Surface(
        modifier = modifier,
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = model.label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Unspecified,
            modifier = Modifier.padding(
                horizontal = dimensions.horizontal,
                vertical = dimensions.vertical,
            ),
        )
    }
}
