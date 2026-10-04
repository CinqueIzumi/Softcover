package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.presentation.model.SettingsChoiceRowUiModel

/**
 * One flat, radio-semantics [SettingsRow], shared by the date-notation rows (label + today's example,
 * tabular-numeral) and the desktop UI-scale rows (label only, [SettingsChoiceRowUiModel.example] `null`). The active row is
 * marked by a `primary`-tinted label and a trailing check glyph, whose 24dp slot stays reserved
 * (transparent tint) when unselected so the label width never shifts on selection.
 */
@Composable
internal fun SettingsSelectableRow(
    model: SettingsChoiceRowUiModel<*>,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    SettingsRow(
        title = model.label,
        titleColor = if (model.selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        secondary = model.example,
        secondaryStyle = MaterialTheme.editorialTypography.bodySmall.copy(
            letterSpacing = 0.3.sp,
            fontFeatureSettings = "tnum",
        ),
        trailing = {
            val checkIcon = drawableIconResource(
                icon = SoftcoverIcon.Check,
                contentDescription = "",
            )

            Icon(
                painter = checkIcon.getIconPainter(),
                contentDescription = checkIcon.contentDescription,
                tint = if (model.selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                modifier = Modifier.size(24.dp),
            )
        },
        interactionSource = interactionSource,
        semantics = Modifier.selectable(
            selected = model.selected,
            interactionSource = interactionSource,
            indication = null,
            role = Role.RadioButton,
            onClick = onClick,
        ),
    )
}
