package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun <K> SheetRow(
    model: SheetRowUiModel<K>,
    onEvent: (SheetRowEvent<K>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHaptics.current

    Column(modifier = modifier) {
        Divider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor()
                .selectable(
                    selected = model.selected,
                    role = Role.RadioButton,
                    onClick = {
                        if (model.selected.not()) {
                            haptics.select()
                        }

                        onEvent(SheetRowEvent.Selected(key = model.key))
                    },
                )
                .padding(vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            val ink = if (model.selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }

            if (model.leadingIcon != null) {
                val leadingIcon = drawableIconResource(
                    icon = model.leadingIcon,
                    contentDescription = "",
                )

                Icon(
                    painter = leadingIcon.getIconPainter(),
                    contentDescription = leadingIcon.contentDescription,
                    tint = ink,
                    modifier = Modifier.size(22.dp),
                )
            }

            SoftcoverText(
                text = model.label,
                style = MaterialTheme.editorialTypography.titleLarge.copy(fontStyle = FontStyle.Italic),
                color = ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            if (model.count != null) {
                SoftcoverText(
                    text = model.count.text,
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics { contentDescription = model.count.description },
                )
            }

            Box(
                modifier = Modifier.size(18.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (model.selected) {
                    val check = drawableIconResource(
                        icon = SoftcoverIcon.Check,
                        contentDescription = "",
                    )

                    Icon(
                        painter = check.getIconPainter(),
                        contentDescription = check.contentDescription,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
