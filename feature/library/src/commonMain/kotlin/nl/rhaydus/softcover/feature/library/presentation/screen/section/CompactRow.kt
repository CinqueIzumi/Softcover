package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleCombinedClickable
import nl.rhaydus.softcover.core.component.badge.Badge
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun CompactRow(
    title: String,
    authorName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    deadlineBadge: BadgeUiModel? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerHandCursor()
            .pressScaleCombinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSelectionMode) {
                SelectionLeadingIcon(isSelected = isSelected)

                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.editorialTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (authorName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "By $authorName",
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (deadlineBadge != null) {
                Spacer(modifier = Modifier.width(8.dp))

                Badge(model = deadlineBadge)
            }

            if (trailing != null) {
                Spacer(modifier = Modifier.width(8.dp))

                trailing()
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
