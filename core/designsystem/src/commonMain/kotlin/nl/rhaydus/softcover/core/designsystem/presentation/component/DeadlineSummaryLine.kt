package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.secondsToHm
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineStatus
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit

/**
 * The deadline date paired with the pace needed to still make it — "28 Aug • 18 pages/day", or the
 * status label alone once the deadline has passed. [foreground], when supplied, overrides the
 * `onSurfaceVariant` ink with a single fixed colour; the Reading screen's featured-hero card passes it
 * because the line sits on that card's blurred-cover backdrop rather than a flat surface.
 *
 * **Give this the full width of its row.** It is deliberately single-line: the date comes from the
 * reader's own [DateStyle] and the pace is unbounded (a short deadline on a long book reads "1149
 * pages/day"), so the string's width is not something a caller can predict — and it grows again with
 * the system font scale. Placed in a narrow column it wraps, which also drags the calendar glyph out
 * of alignment against the resulting two-line block. Callers therefore render it beneath their row's
 * content rather than inside a weighted text column; the [maxLines] here is the backstop for the
 * extreme font scales where even the full width isn't enough, not the layout plan.
 */
@Composable
fun DeadlineSummaryLine(
    progress: DeadlineProgress,
    dateStyle: DateStyle,
    modifier: Modifier = Modifier,
    foreground: Color? = null,
) {
    val contentColor = foreground ?: MaterialTheme.colorScheme.onSurfaceVariant
    val status = progress.status
    val dateText = dateStyle.formatter.format(progress.deadline)

    val paceText = if (status == DeadlineStatus.Expired) {
        status.label
    } else {
        val pace = ceilToInt(progress.requiredPerDay)
        when (progress.unit) {
            DeadlineUnit.PAGES -> {
                val pageLabel = if (pace == 1) "page" else "pages"
                "$pace $pageLabel/day"
            }

            DeadlineUnit.SECONDS -> "${secondsToHm(pace)}/day"
        }
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
            text = "$dateText • $paceText",
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun ceilToInt(value: Float): Int {
    val rounded = value.toInt()

    return if (value > rounded) rounded + 1 else rounded
}
