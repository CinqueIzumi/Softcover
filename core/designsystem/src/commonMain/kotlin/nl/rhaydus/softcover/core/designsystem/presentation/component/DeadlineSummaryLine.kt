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
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.secondsToHm
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineStatus
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit

@Composable
fun DeadlineSummaryLine(
    progress: DeadlineProgress,
    dateStyle: DateStyle,
    modifier: Modifier = Modifier,
) {
    val status = progress.status
    val dateText = dateStyle.formatter.format(progress.deadline)

    val paceText = if (status == DeadlineStatus.Expired) {
        status.label
    } else {
        val pace = ceilToInt(progress.requiredPerDay)
        when (progress.unit) {
            DeadlineUnit.PAGES -> {
                val pageLabel = if (pace == 1) "page" else "pages"
                "$pace $pageLabel / day"
            }

            DeadlineUnit.SECONDS -> "${secondsToHm(pace)} / day"
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )

        Text(
            text = "$dateText • $paceText",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun ceilToInt(value: Float): Int {
    val rounded = value.toInt()

    return if (value > rounded) rounded + 1 else rounded
}
