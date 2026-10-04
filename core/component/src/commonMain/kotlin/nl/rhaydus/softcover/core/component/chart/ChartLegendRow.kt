package nl.rhaydus.softcover.core.component.chart

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.text.SoftcoverText

@Composable
internal fun ChartLegendRow(
    label: String,
    share: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()

        SoftcoverText(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            isLoading = isLoading,
            standIn = "Label name",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(12.dp))

        SoftcoverText(
            text = share,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            isLoading = isLoading,
            standIn = "00%",
        )
    }
}
