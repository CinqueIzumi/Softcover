package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The pace-nudge ribbon, fused to the featured hero card's top edge (design-system.md §5 "Pace-nudge
 * ribbon" — formerly an in-flow row above the card). `primaryContainer` fill, an info glyph, the
 * italic message, and a × dismiss; the outer card's own rounded clip gives it matching top corners.
 */
@Composable
internal fun PaceNudgeRibbon(
    text: String,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val infoIcon = drawableIconResource(
            icon = SoftcoverIcon.Info,
            contentDescription = "",
        )

        Icon(
            painter = infoIcon.getIconPainter(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(18.dp),
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f),
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(22.dp),
        ) {
            val closeIcon = drawableIconResource(
                icon = SoftcoverIcon.Close,
                contentDescription = "Dismiss pace nudge",
            )

            Icon(
                painter = closeIcon.getIconPainter(),
                contentDescription = closeIcon.contentDescription,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@StandardPreview
@Composable
private fun PaceNudgeRibbonPreview() {
    SoftcoverTheme {
        PaceNudgeRibbon(
            text = "Read 24 pages today to stay on pace.",
            onDismiss = {},
        )
    }
}
