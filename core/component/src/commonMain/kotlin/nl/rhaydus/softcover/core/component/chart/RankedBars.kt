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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText

@Composable
fun RankedBars(
    model: RankedBarsUiModel,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(3.dp)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        model.rows.forEach { row ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SoftcoverText(
                        text = row.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        isLoading = model.isLoading,
                        standIn = "Genre name",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    SoftcoverText(
                        text = row.share,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        isLoading = model.isLoading,
                        standIn = "00%",
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f))
                        .skeletonMask(
                            visible = model.isLoading,
                            shape = shape,
                        ),
                ) {
                    if (row.fraction > 0f) {
                        val fill = row.fraction.coerceIn(0.015f..1f)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fill)
                                .fillMaxHeight()
                                .clip(shape)
                                .background(row.tone.color()),
                        )
                    }
                }
            }
        }
    }
}
