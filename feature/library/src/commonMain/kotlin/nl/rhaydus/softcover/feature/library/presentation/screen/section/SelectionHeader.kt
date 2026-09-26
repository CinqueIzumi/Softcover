package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.UserBookStatus

/**
 * The masthead's selection-mode replacement (redesign brief): a 38dp `surfaceContainerHigh` × circle
 * to exit, an italic "N selected" headline, then a row of labelled Move / Add-to-list pills (each
 * `surfaceContainerHigh`, 44dp tall, sharing the row's width) and a 44dp trash circle. Move keeps its
 * existing shelf-target dropdown; behavior otherwise unchanged from the pre-redesign icon-row header.
 */
@Composable
internal fun SelectionHeader(
    selectedCount: Int,
    bulkActionInProgress: Boolean,
    isMoveMenuExpanded: Boolean,
    onExit: () -> Unit,
    onMoveMenuExpandedChange: (Boolean) -> Unit,
    onMoveShelf: (UserBookStatus) -> Unit,
    onAddToListClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SelectionExitButton(
                onClick = onExit,
                enabled = bulkActionInProgress.not(),
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "$selectedCount selected",
                style = MaterialTheme.editorialTypography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                SelectionActionPill(
                    label = "Move",
                    icon = SoftcoverIcon.Bookmark,
                    enabled = bulkActionInProgress.not(),
                    onClick = { onMoveMenuExpandedChange(true) },
                    modifier = Modifier.fillMaxWidth(),
                )

                DropdownMenu(
                    expanded = isMoveMenuExpanded,
                    onDismissRequest = { onMoveMenuExpandedChange(false) },
                ) {
                    SelectionShelfTargets.forEach { (status, label) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                            onClick = { onMoveShelf(status) },
                        )
                    }
                }
            }

            SelectionActionPill(
                label = "Add to list",
                icon = SoftcoverIcon.BookmarkAdd,
                enabled = bulkActionInProgress.not(),
                onClick = onAddToListClick,
                modifier = Modifier.weight(1f),
            )

            SelectionTrashButton(
                onClick = onRemoveClick,
                enabled = bulkActionInProgress.not(),
            )
        }
    }
}

@Composable
private fun SelectionExitButton(
    onClick: () -> Unit,
    enabled: Boolean,
) {
    DesktopTooltip(text = "Exit selection mode") {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(percent = 50),
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .pointerHandCursor()
                .size(38.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                val exitIcon = drawableIconResource(
                    icon = SoftcoverIcon.Close,
                    contentDescription = "Exit selection mode",
                )

                Icon(
                    painter = exitIcon.getIconPainter(),
                    contentDescription = exitIcon.contentDescription,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun SelectionActionPill(
    label: String,
    icon: SoftcoverIcon,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(percent = 50),
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .pointerHandCursor()
            .height(44.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            val resolvedIcon = drawableIconResource(
                icon = icon,
                contentDescription = "",
            )

            Icon(
                painter = resolvedIcon.getIconPainter(),
                contentDescription = resolvedIcon.contentDescription,
                modifier = Modifier.size(17.dp),
            )

            Spacer(modifier = Modifier.width(7.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun SelectionTrashButton(
    onClick: () -> Unit,
    enabled: Boolean,
) {
    DesktopTooltip(text = "Remove selected books from library") {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(percent = 50),
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .pointerHandCursor()
                .size(44.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                val removeIcon = drawableIconResource(
                    icon = SoftcoverIcon.Delete,
                    contentDescription = "Remove selected books from library",
                )

                Icon(
                    painter = removeIcon.getIconPainter(),
                    contentDescription = removeIcon.contentDescription,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private val SelectionShelfTargets: List<Pair<UserBookStatus, String>> = listOf(
    UserBookStatus.WANT_TO_READ to "Move to Want to Read",
    UserBookStatus.CURRENTLY_READING to "Move to Currently Reading",
    UserBookStatus.READ to "Mark as Read",
)
