package nl.rhaydus.softcover.core.component.badge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.ReadingHeroBackdropForeground

/**
 * The deadline date paired with the pace needed to still make it — "28 Aug • 18 pages/day", or the
 * status label alone once the deadline has passed — both already resolved onto [model] by the
 * mapper (§ 7.2 R4); this component only lays the calendar glyph and the string beside each other.
 *
 * **Give this the full width of its row.** It is deliberately single-line: the date comes from the
 * reader's own date style and the pace is unbounded (a short deadline on a long book reads "1149
 * pages/day"), so the string's width is not something a caller can predict — and it grows again with
 * the system font scale. Placed in a narrow column it wraps, which also drags the calendar glyph out
 * of alignment against the resulting two-line block. Callers therefore render it beneath their row's
 * content rather than inside a weighted text column; the `maxLines` here is the backstop for the
 * extreme font scales where even the full width isn't enough, not the layout plan.
 */
@Composable
fun DeadlineSummaryLine(
    model: DeadlineSummaryUiModel,
    modifier: Modifier = Modifier,
) {
    val contentColor = when (model.tone) {
        DeadlineSummaryTone.OnSurface -> MaterialTheme.colorScheme.onSurfaceVariant
        DeadlineSummaryTone.OnHeroBackdrop -> ReadingHeroBackdropForeground
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val icon = drawableIconResource(
            icon = SoftcoverIcon.DateRange,
            contentDescription = "",
        )

        Icon(
            painter = icon.getIconPainter(),
            contentDescription = icon.contentDescription,
            tint = contentColor,
            modifier = Modifier.size(14.dp),
        )

        Text(
            text = "${model.dateText} • ${model.paceText}",
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
