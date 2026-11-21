package nl.rhaydus.softcover.core.component.yourcopy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.theme.Corners
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.text.FadeThroughText
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
fun <K> YourCopyPanel(
    model: YourCopyPanelUiModel<K>,
    onEvent: (YourCopyPanelEvent<K>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Corners.xl,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            model.rows.forEachIndexed { index, row ->
                if (index > 0) Divider()

                YourCopyRow(
                    model = row,
                    onClick = { onEvent(YourCopyPanelEvent.RowClicked(key = row.key)) },
                )
            }
        }
    }
}

@Composable
private fun <K> YourCopyRow(
    model: YourCopyRowUiModel<K>,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick)
            .padding(
                horizontal = Spacing.md,
                vertical = Spacing.sm,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        val icon = drawableIconResource(
            icon = model.icon,
            contentDescription = "",
        )

        Icon(
            painter = icon.getIconPainter(),
            contentDescription = icon.contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            SoftcoverText(
                text = model.label,
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            when (val value = model.value) {
                is YourCopyValue.Set -> FadeThroughText(
                    text = value.text,
                    style = MaterialTheme.editorialTypography.body,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                is YourCopyValue.Empty -> FadeThroughText(
                    text = value.invitation,
                    style = MaterialTheme.editorialTypography.body.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (model.trailing != null) {
            SoftcoverText(
                text = model.trailing,
                style = MaterialTheme.editorialTypography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}
