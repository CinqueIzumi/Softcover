package nl.rhaydus.softcover.core.component.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.state.clippedSkeletonMask
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

@Composable
fun ProportionChart(
    model: ProportionChartUiModel,
    modifier: Modifier = Modifier,
) {
    val barShape = Corners.sm

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clippedSkeletonMask(
                    visible = model.isLoading,
                    shape = barShape,
                ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxxs),
        ) {
            model.segments.filter { it.fraction > 0f }.forEach { segment ->
                Box(
                    modifier = Modifier
                        .weight(segment.fraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(segment.tone.color(colors = MaterialTheme.colorScheme)),
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        model.segments.forEach { segment ->
            Divider()

            ChartLegendRow(
                slice = segment,
                isLoading = model.isLoading,
                swatch = true,
                modifier = Modifier.padding(vertical = Spacing.xs),
            )
        }
    }
}
