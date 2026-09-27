package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

@Composable
internal fun SettingsSidebarRow(
    label: String,
    icon: SoftcoverIcon,
    selected: Boolean,
    showTrailingArrow: Boolean,
    onClick: () -> Unit,
) {
    val container = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent

    val content = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val interactionSource = remember { MutableInteractionSource() }
    val rowShape = RoundedCornerShape(10.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 2.dp,
            )
            .pointerHandCursor()
            .hoverHighlight(
                interactionSource = interactionSource,
                shape = rowShape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        color = container,
        contentColor = content,
        shape = rowShape,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 11.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val leadingIcon = drawableIconResource(
                icon = icon,
                contentDescription = "",
            )

            Icon(
                painter = leadingIcon.getIconPainter(),
                contentDescription = leadingIcon.contentDescription,
                modifier = Modifier.size(20.dp),
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                ),
                modifier = Modifier.weight(1f),
            )

            if (showTrailingArrow) {
                val arrowIcon = drawableIconResource(
                    icon = SoftcoverIcon.KeyboardArrowRight,
                    contentDescription = "",
                )

                Icon(
                    painter = arrowIcon.getIconPainter(),
                    contentDescription = arrowIcon.contentDescription,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
