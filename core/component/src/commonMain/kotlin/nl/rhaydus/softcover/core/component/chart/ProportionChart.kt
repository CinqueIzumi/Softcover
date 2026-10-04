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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.state.skeletonMask

@Composable
fun ProportionChart(
    model: ProportionChartUiModel,
    modifier: Modifier = Modifier,
) {
    val barShape = RoundedCornerShape(6.dp)
    val swatchShape = RoundedCornerShape(3.dp)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(barShape)
                .skeletonMask(
                    visible = model.isLoading,
                    shape = barShape,
                ),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            model.segments.filter { it.fraction > 0f }.forEach { segment ->
                Box(
                    modifier = Modifier
                        .weight(segment.fraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(segment.tone.color()),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        model.segments.forEach { segment ->
            Divider()

            ChartLegendRow(
                label = segment.label,
                share = segment.share,
                isLoading = model.isLoading,
                modifier = Modifier.padding(vertical = 7.dp),
                leading = {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(swatchShape)
                            .background(segment.tone.color())
                            .skeletonMask(
                                visible = model.isLoading,
                                shape = swatchShape,
                            ),
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                },
            )
        }
    }
}
