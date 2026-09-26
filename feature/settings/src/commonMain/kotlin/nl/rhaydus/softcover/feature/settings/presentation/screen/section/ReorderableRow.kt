package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.presentation.model.LibraryTabEntry

private const val HIDDEN_ROW_ALPHA = 0.52f

/**
 * One flat, borderless row: a leading grip/pin column, the name (a leading 6dp primary dot for a
 * list), an italic tabular count line, and a trailing eye toggle when [LibraryTabEntry.canHide]. There
 * is no per-row click target — only the grip (drag) and the eye (visibility) are interactive — so the
 * row's own "press wash" reflects [isDragging] rather than a tap, matching the design system's
 * settings-row press-wash convention (an `animateColorAsState` gated by [playDecorativeMotion]).
 */
@Composable
internal fun ReorderableRow(
    entry: LibraryTabEntry,
    hidden: Boolean,
    isDragging: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playMotion = playDecorativeMotion()

    val rowBackground by animateColorAsState(
        targetValue = if (isDragging && playMotion) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            Color.Transparent
        },
        label = "libraryTabRowWash",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(rowBackground)
            .alpha(if (hidden) HIDDEN_ROW_ALPHA else 1f)
            .padding(
                start = 14.dp,
                top = 13.dp,
                end = 18.dp,
                bottom = 13.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GripOrPinGlyph(
            entry = entry,
            isDragging = isDragging,
            modifier = modifier,
        )

        Column(modifier = Modifier.weight(1f)) {
            RowLabel(
                entry = entry,
                hidden = hidden,
            )

            Spacer(modifier = Modifier.height(2.dp))

            RowCountLine(
                entry = entry,
                hidden = hidden,
            )
        }

        if (entry.canHide) {
            EyeToggle(
                hidden = hidden,
                label = entry.label,
                onClick = onToggle,
            )
        }
    }
}

/**
 * The leading 26dp grip column: a drag-handle glyph (outline, primary while dragging) when
 * [LibraryTabEntry.isReorderable], else a demoted pin glyph standing in for "fixed first" — carries no
 * drag [modifier] in that case, since the "All" entry can neither move nor be moved past.
 */
@Composable
private fun GripOrPinGlyph(
    entry: LibraryTabEntry,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.width(26.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (entry.isReorderable) {
            val gripIcon = drawableIconResource(
                icon = SoftcoverIcon.DragHandle,
                contentDescription = "Reorder ${entry.label}",
            )

            DesktopTooltip(text = "Drag to reorder") {
                Icon(
                    painter = gripIcon.getIconPainter(),
                    contentDescription = gripIcon.contentDescription,
                    tint = if (isDragging) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier
                        .pointerHandCursor()
                        .size(20.dp),
                )
            }
        } else {
            val pinIcon = drawableIconResource(
                icon = SoftcoverIcon.Pin,
                contentDescription = "${entry.label} always shown first",
            )

            Icon(
                painter = pinIcon.getIconPainter(),
                contentDescription = pinIcon.contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

@Composable
private fun RowLabel(
    entry: LibraryTabEntry,
    hidden: Boolean,
) {
    val textColor = if (hidden) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (entry.isList) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )

            Spacer(modifier = Modifier.width(7.dp))
        }

        Text(
            text = entry.label,
            style = MaterialTheme.editorialTypography.titleSmall.copy(fontSize = 15.sp),
            color = textColor,
        )
    }
}

@Composable
private fun RowCountLine(
    entry: LibraryTabEntry,
    hidden: Boolean,
) {
    Text(
        text = if (hidden) "Hidden from tabs" else "${entry.count} titles",
        style = MaterialTheme.editorialTypography.bodySmall.copy(
            fontSize = 12.5.sp,
            lineHeight = 16.sp,
            fontFeatureSettings = "tnum",
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * The trailing 38dp circular visibility toggle — the single control that shows/hides a row, replacing
 * both the old per-row `Switch` and the (already-absent) overflow menu. Mirrors the tag editor sheet's
 * spoiler-toggle eye affordance: hand-cursor + press-scale + a [DesktopTooltip]-carried label.
 */
@Composable
private fun EyeToggle(
    hidden: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    val description = if (hidden) "Show $label on the library tabs" else "Hide $label from the library tabs"
    val icon = if (hidden) SoftcoverIcon.VisibilityOff else SoftcoverIcon.Visibility
    val tint = if (hidden) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
    val resolvedIcon = drawableIconResource(
        icon = icon,
        contentDescription = description,
    )

    DesktopTooltip(text = description) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .pointerHandCursor()
                .pressScaleClickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = resolvedIcon.getIconPainter(),
                contentDescription = resolvedIcon.contentDescription,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
