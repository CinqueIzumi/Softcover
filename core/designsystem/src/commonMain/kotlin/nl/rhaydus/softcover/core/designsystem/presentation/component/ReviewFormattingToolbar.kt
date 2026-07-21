package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Inline formatting controls for the review editor: three pill toggles that apply bold, italic, or the
 * spoiler mark to the current text selection. They follow the app's chip anatomy (§3.4 / inline filter
 * chip strip) — fully-rounded, `secondaryContainer` when active and `surfaceContainer` when idle — so
 * the row reads as one deliberate control rather than loose buttons. Each toggle reflects whether the
 * whole selection already carries that mark, and the whole row dims to signal it is inert when there is
 * no selection to act on. Labels are words (the bold/italic ones carry their own weight/slant to preview
 * the effect) rather than icons, matching the editor's editorial voice and avoiding icon assets.
 */
@Composable
internal fun ReviewFormattingToolbar(
    isBold: Boolean,
    isItalic: Boolean,
    isSpoiler: Boolean,
    enabled: Boolean,
    onToggle: (ReviewMarkType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.alpha(if (enabled) 1f else 0.45f),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FormatChip(
            label = "Bold",
            fontWeight = FontWeight.Bold,
            active = isBold,
            enabled = enabled,
            onClick = { onToggle(ReviewMarkType.BOLD) },
        )

        FormatChip(
            label = "Italic",
            fontStyle = FontStyle.Italic,
            active = isItalic,
            enabled = enabled,
            onClick = { onToggle(ReviewMarkType.ITALIC) },
        )

        FormatChip(
            label = "Spoiler",
            active = isSpoiler,
            enabled = enabled,
            onClick = { onToggle(ReviewMarkType.SPOILER) },
        )
    }
}

@Composable
private fun FormatChip(
    label: String,
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
) {
    val containerColor = if (active) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val contentColor = if (active) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        enabled = enabled,
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = fontWeight,
                fontStyle = fontStyle ?: FontStyle.Normal,
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
