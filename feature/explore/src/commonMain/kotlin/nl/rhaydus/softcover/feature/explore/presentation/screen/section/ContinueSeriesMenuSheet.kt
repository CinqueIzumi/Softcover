package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesMenuOptionUiModel
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesMenuSheetUiModel

/**
 * The shared dismiss-options sheet host for an "up next" series book. Runs the picked option's dismiss
 * action through [runAction] so both the mobile carousel and the desktop grid open an identical sheet,
 * and closes through [LocalModalSheetDismiss] so each form animates out the way it should. [onDismiss]
 * clears the host's selected book once the sheet has closed.
 */
@Composable
internal fun ContinueSeriesMenuSheet(
    model: ContinueSeriesMenuSheetUiModel,
    runAction: (ExploreAction) -> Unit,
    onDismiss: () -> Unit,
) {
    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.Dismissed -> onDismiss()

                SheetScaffoldEvent.PrimaryClicked,
                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
    ) {
        val dismiss = LocalModalSheetDismiss.current

        model.options.forEach { option ->
            DismissSheetOption(
                model = option,
                onClick = {
                    runAction(option.dismissAction)

                    dismiss()
                },
            )
        }
    }
}

@Composable
private fun DismissSheetOption(
    model: ContinueSeriesMenuOptionUiModel,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerHandCursor()
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        val iconResource = drawableIconResource(
            icon = model.icon,
            contentDescription = "",
        )

        Icon(
            painter = iconResource.getIconPainter(),
            contentDescription = iconResource.contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SoftcoverText(
                text = model.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            SoftcoverText(
                text = model.description,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
