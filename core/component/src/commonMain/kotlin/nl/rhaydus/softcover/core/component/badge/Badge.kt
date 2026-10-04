package nl.rhaydus.softcover.core.component.badge

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

/**
 * A small pill of chrome reporting a status in one word or a short phrase — a reading deadline's
 * pace, an edition's release date, or an "Owned"/"TRACKING NOW" mark.
 */
@Composable
fun Badge(
    model: BadgeUiModel,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (model.tone) {
        BadgeTone.Primary -> scheme.primary to scheme.onPrimary
        BadgeTone.PrimaryContainer -> scheme.primaryContainer to scheme.onPrimaryContainer
        BadgeTone.ErrorContainer -> scheme.errorContainer to scheme.onErrorContainer
        BadgeTone.Neutral -> scheme.surfaceContainerHigh to scheme.onSurfaceVariant
        BadgeTone.Scrim -> Color.Black.copy(alpha = 0.55f) to Color.White
    }
    val padding = when (model.size) {
        BadgeSize.Compact -> PaddingValues(
            horizontal = 6.dp,
            vertical = 2.dp,
        )

        BadgeSize.Regular -> PaddingValues(
            horizontal = 8.dp,
            vertical = 4.dp,
        )
    }
    val style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)

    Surface(
        modifier = modifier,
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            modifier = Modifier.padding(padding),
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
                    modifier = Modifier.size(12.dp),
                )

                Spacer(modifier = Modifier.width(4.dp))
            }

            SoftcoverText(
                text = model.label,
                style = style,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
