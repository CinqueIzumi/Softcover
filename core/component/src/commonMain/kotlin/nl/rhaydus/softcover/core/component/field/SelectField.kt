package nl.rhaydus.softcover.core.component.field

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

@Composable
fun SelectField(
    model: SelectFieldUiModel,
    onEvent: (SelectFieldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = outlinedFieldColors()
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = OutlinedTextFieldDefaults.MinHeight)
            .pointerHandCursor()
            .pressScaleClickable(
                onClick = { onEvent(SelectFieldEvent.Clicked(key = model.key)) },
                role = Role.Button,
                hoverShape = outlinedFieldShape,
                interactionSource = interactionSource,
            )
            .semantics { contentDescription = model.contentDescription },
        contentAlignment = Alignment.CenterStart,
    ) {
        OutlinedTextFieldDefaults.Container(
            enabled = true,
            isError = false,
            interactionSource = interactionSource,
            colors = colors,
            shape = outlinedFieldShape,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            SoftcoverText(
                text = model.value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            val chevron = drawableIconResource(
                icon = SoftcoverIcon.KeyboardArrowDown,
                contentDescription = "",
            )

            Icon(
                painter = chevron.getIconPainter(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
