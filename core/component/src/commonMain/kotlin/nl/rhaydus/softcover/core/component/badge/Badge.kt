package nl.rhaydus.softcover.core.component.badge

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import nl.rhaydus.softcover.core.component.text.SoftcoverText
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
    val colors = BadgeColors.of(
        tone = model.tone,
        scheme = MaterialTheme.colorScheme,
    )
    val padding = BadgeDimensions.paddingFor(size = model.size)
    val style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)

    Surface(
        modifier = modifier,
        color = colors.container,
        contentColor = colors.content,
        shape = BadgeDimensions.shape,
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
                    modifier = Modifier.size(BadgeDimensions.iconSize),
                )

                Spacer(modifier = Modifier.width(BadgeDimensions.iconGap))
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
