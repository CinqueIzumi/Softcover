package nl.rhaydus.softcover.core.component.badge

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

/**
 * A small pill of chrome reporting a status in one word or a short phrase — a reading deadline's
 * pace, an edition's release date, or an "Owned"/"TRACKING NOW" mark. Inert: a badge is read, never
 * tapped, so it takes no event lambda (§ 7.2 R1 — there is nothing to hoist when there is nothing to
 * click).
 */
@Composable
fun Badge(
    model: BadgeUiModel,
    modifier: Modifier = Modifier,
) {
    val (container, content) = when (model.tone) {
        BadgeTone.Primary -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        BadgeTone.PrimaryContainer ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        BadgeTone.ErrorContainer ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        BadgeTone.Muted -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        BadgeTone.Neutral ->
            MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurfaceVariant
        BadgeTone.Scrim -> Color.Black.copy(alpha = SCRIM_ALPHA) to Color.White
    }

    val dimensions = BadgeDimensions.forVariant(variant = model.variant)

    val labelStyle = MaterialTheme.typography.labelSmall
    val resolvedStyle = labelStyle.copy(
        fontWeight = dimensions.fontWeight ?: labelStyle.fontWeight,
        letterSpacing = if (dimensions.letterSpacing == TextUnit.Unspecified) {
            labelStyle.letterSpacing
        } else {
            dimensions.letterSpacing
        },
    )

    Surface(
        modifier = modifier,
        color = container,
        contentColor = content,
        shape = dimensions.shape,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = dimensions.horizontal,
                vertical = dimensions.vertical,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (model.icon != null) {
                val icon = drawableIconResource(
                    icon = model.icon,
                    contentDescription = "",
                )

                Icon(
                    painter = icon.getIconPainter(),
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSize),
                )

                Spacer(modifier = Modifier.width(dimensions.iconGap))
            }

            Text(
                text = model.label,
                style = resolvedStyle,
                color = Color.Unspecified,
            )
        }
    }
}

private const val SCRIM_ALPHA = 0.55f
