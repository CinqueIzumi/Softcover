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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.state.LibrarySelectionPills

/**
 * The masthead's selection-mode replacement: an exit circle, an italic "N selected" headline, then a row of
 * labelled Move / Add-to-list pills sharing the row's width and a trash circle. Move anchors the
 * shelf-target dropdown.
 */
@Composable
internal fun SelectionHeader(
    selectedCount: StatNumberUiModel,
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
            .padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PillButton(
                model = pills.exit,
                onEvent = { onExit() },
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                StatNumber(
                    model = selectedCount,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
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
                    pills.moveTargets.forEach { (status, label) ->
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
