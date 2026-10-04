package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.state.LibrarySelectionPills

/**
 * The masthead's selection-mode replacement: an exit circle, an italic "N selected" headline, then a row of
 * labelled Move / Add-to-list pills sharing the row's width and a trash circle. Move anchors the
 * shelf-target dropdown.
 */
@Composable
internal fun SelectionHeader(
    selectedCount: Int,
    pills: LibrarySelectionPills,
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
            PillButton(
                model = pills.exit,
                onEvent = { onExit() },
            )

            Spacer(modifier = Modifier.width(14.dp))

            SoftcoverText(
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
                PillButton(
                    model = pills.move,
                    onEvent = { onMoveMenuExpandedChange(true) },
                    modifier = Modifier.fillMaxWidth(),
                )

                DropdownMenu(
                    expanded = isMoveMenuExpanded,
                    onDismissRequest = { onMoveMenuExpandedChange(false) },
                ) {
                    SelectionShelfTargets.forEach { (status, label) ->
                        DropdownMenuItem(
                            text = {
                                SoftcoverText(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                            onClick = { onMoveShelf(status) },
                        )
                    }
                }
            }

            PillButton(
                model = pills.addToList,
                onEvent = { onAddToListClick() },
                modifier = Modifier.weight(1f),
            )

            PillButton(
                model = pills.remove,
                onEvent = { onRemoveClick() },
            )
        }
    }
}

private val SelectionShelfTargets: List<Pair<UserBookStatus, String>> = listOf(
    UserBookStatus.WANT_TO_READ to "Move to Want to Read",
    UserBookStatus.CURRENTLY_READING to "Move to Currently Reading",
    UserBookStatus.READ to "Mark as Read",
)
