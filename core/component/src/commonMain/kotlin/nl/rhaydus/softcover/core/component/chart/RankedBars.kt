package nl.rhaydus.softcover.core.component.chart

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.motion

@Composable
fun RankedBars(
    model: RankedBarsUiModel,
    modifier: Modifier = Modifier,
) {
    val shape = Corners.xxs

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        model.rows.forEachIndexed { index, row ->
            key(if (model.isLoading) index else row.label) {
                val fillPercent = animateFloatAsState(
                    targetValue = if (model.isLoading || row.fraction <= 0f) 0f else row.fraction.coerceIn(0.015f..1f) * 100f,
                    animationSpec = MaterialTheme.motion.valueChange,
                    label = "RankedBarsFill",
                )

                Column {
                    ChartLegendRow(
                        slice = row,
                        isLoading = model.isLoading,
                        swatch = false,
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

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
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .layout { measurable, constraints ->
                                    val fillWidth = (constraints.maxWidth * fillPercent.value / 100f).roundToInt()

                                    val placeable = measurable.measure(
                                        constraints.copy(
                                            minWidth = fillWidth,
                                            maxWidth = fillWidth,
                                        ),
                                    )

                                    layout(
                                        width = placeable.width,
                                        height = placeable.height,
                                    ) {
                                        placeable.place(IntOffset.Zero)
                                    }
                                }
                                .clip(shape)
                                .background(row.tone.color(colors = MaterialTheme.colorScheme)),
                        )
                    }
                }
            }
        }
    }
}
