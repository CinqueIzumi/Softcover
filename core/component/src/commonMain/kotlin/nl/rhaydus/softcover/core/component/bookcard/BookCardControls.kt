package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.badge.Badge
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.control.Toggle
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MonogramCoverForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MonogramCoverInk

@Composable
internal fun BookCardTrailingControl(
    trailing: BookCardTrailing,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.skeletonMask(visible = isLoading)) {
        when (trailing) {
            is BookCardTrailing.Badge -> Badge(model = trailing.model)

            is BookCardTrailing.Restore -> RestoreLabel(label = trailing.label)

            is BookCardTrailing.Bookmark -> Toggle(
                model = trailing.toggle,
                onEvent = { onClick() },
            )

            is BookCardTrailing.SetProgress -> PillButton(
                model = trailing.pill,
                onEvent = { onClick() },
            )
        }
    }
}

/**
 * The circle sits in the touch target's top-end corner, not its centre, so it lands 6dp in from the
 * cover's corner.
 */
@Composable
internal fun BookCardMenuButton(
    contentDescription: String,
    onClick: () -> Unit,
) {
    val icon = drawableIconResource(
        icon = SoftcoverIcon.MoreVert,
        contentDescription = contentDescription,
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier.pointerHandCursor(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.TopEnd,
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(
                        color = MonogramCoverInk.copy(alpha = 0.42f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = icon.getIconPainter(),
                    contentDescription = icon.contentDescription,
                    tint = MonogramCoverForeground,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun RestoreLabel(label: String) {
    val icon = drawableIconResource(
        icon = SoftcoverIcon.History,
        contentDescription = "",
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = icon.getIconPainter(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )

        SoftcoverText(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
