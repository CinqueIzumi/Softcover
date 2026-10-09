package nl.rhaydus.softcover.core.component.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
fun ColumnChart(
    model: ColumnChartUiModel,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(3.dp)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        model.columns.forEach { column ->
            val isLead = column.tone == ChartTone.Lead

            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (model.isLoading) {
                            Modifier
                        } else {
                            Modifier.clearAndSetSemantics { contentDescription = column.contentDescription }
                        },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (model.showsValues) {
                    Box(
                        modifier = Modifier.height(16.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        column.value?.let { value ->
                            SoftcoverText(
                                text = value,
                                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                                    fontWeight = if (isLead) FontWeight.Bold else FontWeight.SemiBold,
                                ),
                                color = if (isLead) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                isLoading = model.isLoading,
                                standIn = "00",
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.xs))
                }

                Box(
                    modifier = Modifier.height(96.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 20.dp)
                            .fillMaxWidth()
                            .height(
                                if (column.fraction > 0f) {
                                    12.dp + (96.dp - 12.dp) * column.fraction
                                } else {
                                    3.dp
                                },
                            )
                            .clip(shape)
                            .background(column.tone.color(colors = MaterialTheme.colorScheme))
                            .skeletonMask(
                                visible = model.isLoading,
                                shape = shape,
                            ),
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                Box(
                    modifier = Modifier.height(16.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    column.label?.let { label ->
                        SoftcoverText(
                            text = label,
                            style = MaterialTheme.editorialTypography.eyebrowSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            isLoading = model.isLoading,
                            standIn = "'00",
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
