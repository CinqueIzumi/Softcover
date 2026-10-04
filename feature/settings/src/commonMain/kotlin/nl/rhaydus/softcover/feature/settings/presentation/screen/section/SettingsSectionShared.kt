package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.component.control.Toggle
import nl.rhaydus.softcover.core.component.control.ToggleUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * A picker tile is a picture, not a text block, so it stops growing once it is big enough to read —
 * a row of them stretched across a desktop settings pane would read as a row of posters. Shared by
 * the theme and spine-colour pickers, so the two rows are the same size.
 */
internal val PREVIEW_TILE_MAX_WIDTH = 128.dp

/** The gap between picker tiles along a row. */
internal val PREVIEW_TILE_GAP = 14.dp

/** One flat [SettingsRow] toggle: [label] over [gloss] on the left, a [Toggle] switch on the right. */
@Composable
internal fun SettingsToggleRow(
    label: String,
    gloss: String,
    toggle: ToggleUiModel,
    onCheckedChange: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    SettingsRow(
        title = label,
        secondary = gloss,
        trailing = {
            Toggle(
                model = toggle,
                onEvent = {},
            )
        },
        interactionSource = interactionSource,
        semantics = Modifier.toggleable(
            value = toggle.checked,
            interactionSource = interactionSource,
            indication = null,
            enabled = toggle.enabled,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        ),
    )
}

/**
 * One flat, radio-semantics [SettingsRow], shared by the date-notation rows (label + today's example,
 * tabular-numeral) and the desktop UI-scale rows (label only, [example] `null`). The active row is
 * marked by a `primary`-tinted label and a trailing check glyph, whose 24dp slot stays reserved
 * (transparent tint) when unselected so the label width never shifts on selection.
 */
@Composable
internal fun SettingsSelectableRow(
    label: String,
    example: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    SettingsRow(
        title = label,
        titleColor = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        secondary = example,
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
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                modifier = Modifier.size(24.dp),
            )
        },
        interactionSource = interactionSource,
        semantics = Modifier.selectable(
            selected = isSelected,
            interactionSource = interactionSource,
            indication = null,
            role = Role.RadioButton,
            onClick = onClick,
        ),
    )
}
