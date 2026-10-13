package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.component.modifier.tactileSelectable
import nl.rhaydus.softcover.core.component.selection.SelectionCheck
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.tabularFigures
import nl.rhaydus.softcover.feature.settings.presentation.model.SettingsChoiceRowUiModel

/**
 * One flat, radio-semantics [SettingsRow], shared by the date-notation rows (label + today's example,
 * tabular-numeral) and the desktop UI-scale rows (label only, [SettingsChoiceRowUiModel.example] `null`).
 */
@Composable
internal fun SettingsSelectableRow(
    model: SettingsChoiceRowUiModel<*>,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val titleColor by animateColorAsState(
        targetValue = if (model.selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>(),
    )

    SettingsRow(
        title = model.label,
        titleColor = titleColor,
        secondary = model.example,
        secondaryStyle = MaterialTheme.editorialTypography.bodySmall.copy(
            letterSpacing = 0.3.sp,
        ).tabularFigures(),
        trailing = {
            SelectionCheck(
                selected = model.selected,
                size = 24.dp,
            )
        },
        interactionSource = interactionSource,
        semantics = Modifier.tactileSelectable(
            selected = model.selected,
            onClick = onClick,
            role = Role.RadioButton,
            interactionSource = interactionSource,
        ),
    )
}
