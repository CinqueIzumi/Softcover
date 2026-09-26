package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.modifier.noRippleClickable
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

/**
 * One flat toggle row: label over an italic Fraunces gloss on the left, an M3 [Switch] on the right —
 * no card, no "On/Off" caption, no per-row accent. Dividers between rows are drawn by the caller.
 */
@Composable
internal fun SettingsToggleRow(
    label: String,
    gloss: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = gloss,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

/**
 * One flat, radio-semantics selectable row, shared by the date-notation rows (label + today's example,
 * tabular-numeral) and the desktop UI-scale rows (label only, [example] `null`) — no radio circle, no
 * box; the active row is marked only by a `primary`-tinted label and a trailing check glyph.
 */
@Composable
internal fun SettingsSelectableRow(
    label: String,
    example: String?,
    isSelected: Boolean,
    checkContentDescription: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.editorialTypography.titleMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )

            if (example != null) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = example,
                    style = MaterialTheme.editorialTypography.bodySmall.copy(
                        letterSpacing = 0.3.sp,
                        fontFeatureSettings = "tnum",
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (isSelected) {
            Spacer(modifier = Modifier.width(16.dp))

            val checkIcon = drawableIconResource(
                icon = SoftcoverIcon.Check,
                contentDescription = checkContentDescription,
            )

            Icon(
                painter = checkIcon.getIconPainter(),
                contentDescription = checkIcon.contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
