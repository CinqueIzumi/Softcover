package nl.rhaydus.softcover.core.component.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

/**
 * "When did you read this?" (patterns/reading.md, "Backdate a logged action"). A pill
 * defaulting to "Just now" — no backdating, so the submitted `actionAt` is null and the server
 * stamps the mutation with its own current time. Tapping it seeds the picker with the current
 * moment and expands an inline day + time-of-day editor below; a trailing clear glyph resets to
 * "Just now" and collapses. Selected state swaps to `secondaryContainer`, matching the shared
 * pill-chip anatomy (§4 `PillChip`) this reuses at hand-rolled scale for its icon-leading form.
 */
@Composable
internal fun WhenReadRow(
    pickedDateTime: LocalDateTime?,
    onPickedDateTimeChange: (LocalDateTime?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    // The sheet is a short-lived interaction (seconds, not minutes), so a single snapshot of "now"
    // taken at first composition is precise enough to anchor the "no future" clamp below — it does
    // not need to keep ticking while the picker is open.
    val now = remember { currentLocalDateTime() }
    val timeZone = remember { TimeZone.currentSystemDefault() }

    val isCustomized = pickedDateTime != null

    val containerColor = if (isCustomized) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val contentColor = if (isCustomized) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(color = containerColor)
                .pointerHandCursor()
                .pressScaleClickable(
                    onClick = {
                        if (isCustomized.not()) {
                            onPickedDateTimeChange(now)
                        }

                        expanded = expanded.not()
                    },
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            val dateRangeIcon = drawableIconResource(
                icon = SoftcoverIcon.DateRange,
                contentDescription = "",
            )

            Icon(
                painter = dateRangeIcon.getIconPainter(),
                contentDescription = dateRangeIcon.contentDescription,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = pickedDateTime?.let {
                    formatWhenReadLabel(
                        picked = it,
                        now = now,
                    )
                } ?: "Just now",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
            )

            if (isCustomized) {
                Spacer(modifier = Modifier.width(8.dp))

                val clearIcon = drawableIconResource(
                    icon = SoftcoverIcon.Close,
                    contentDescription = "Reset to just now",
                )

                Icon(
                    painter = clearIcon.getIconPainter(),
                    contentDescription = clearIcon.contentDescription,
                    tint = contentColor,
                    modifier = Modifier
                        .size(16.dp)
                        .pointerHandCursor()
                        .pressScaleClickable(
                            onClick = {
                                onPickedDateTimeChange(null)
                                expanded = false
                            },
                        ),
                )
            }
        }

        AnimatedVisibility(
            visible = expanded && isCustomized,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            WhenReadEditor(
                pickedDateTime = pickedDateTime ?: now,
                now = now,
                timeZone = timeZone,
                onPickedDateTimeChange = onPickedDateTimeChange,
            )
        }
    }
}

/** [dayLabelFor], paired with the picked time-of-day. */
private fun formatWhenReadLabel(
    picked: LocalDateTime,
    now: LocalDateTime,
): String {
    val hh = picked.hour.toString().padStart(
        2,
        '0',
    )
    val mm = picked.minute.toString().padStart(
        2,
        '0',
    )

    return "${dayLabelFor(
        picked = picked,
        now = now,
    )}, $hh:$mm"
}
