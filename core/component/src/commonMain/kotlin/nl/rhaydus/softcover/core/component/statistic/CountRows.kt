package nl.rhaydus.softcover.core.component.statistic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.tabularFigures

@Composable
fun CountRows(
    model: CountRowsUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        model.rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SoftcoverText(
                    text = row.label,
                    style = MaterialTheme.editorialTypography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(Spacing.sm))

                SoftcoverText(
                    text = row.count,
                    style = MaterialTheme.typography.labelLarge.tabularFigures(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}
