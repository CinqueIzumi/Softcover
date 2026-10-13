package nl.rhaydus.softcover.core.component.chart

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.FadeThroughText
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.motion

@Composable
fun ColumnChart(
    model: ColumnChartUiModel,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHaptics.current
    val gapPx = with(LocalDensity.current) { Spacing.xs.toPx() }
    var rowWidth by remember { mutableIntStateOf(0) }
    // Scoped so an index the pointer could no longer clear (it is detached while loading) never outlives it.
    var rawActiveIndex by remember(model.isLoading, model.columns.size) { mutableIntStateOf(-1) }
    val activeIndex = if (model.isLoading || rawActiveIndex !in model.columns.indices) -1 else rawActiveIndex
    val activeText = model.columns.getOrNull(activeIndex)?.contentDescription?.takeIf { it.isNotEmpty() }

    Box(modifier = modifier.fillMaxWidth()) {
        ColumnChartRow(
            model = model,
            activeIndex = activeIndex,
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { rowWidth = it.width }
                .columnChartPointer(
                    enabled = model.isLoading.not(),
                    columnCount = model.columns.size,
                    rowWidth = { rowWidth.toFloat() },
                    gapPx = gapPx,
                    haptics = haptics,
                    onActiveChange = { rawActiveIndex = it },
                ),
        )

        ColumnReadout(
            text = activeText,
            centerX = if (activeIndex >= 0) {
                columnCenterX(
                    index = activeIndex,
                    rowWidth = rowWidth.toFloat(),
                    count = model.columns.size,
                    gap = gapPx,
                )
            } else {
                null
            },
            rowWidth = rowWidth.toFloat(),
        )
    }
}

@Composable
private fun ColumnChartRow(
    model: ColumnChartUiModel,
    activeIndex: Int,
    modifier: Modifier = Modifier,
) {
    val shape = Corners.xxs

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        model.columns.forEachIndexed { index, column ->
            val isLead = column.tone == ChartTone.Lead
            val targetHeight = if (column.fraction > 0f) 12f + (96f - 12f) * column.fraction else 3f

            // Loading holds the animation at zero so the real bars grow from the baseline once it ends.
            val grownHeight = animateFloatAsState(
                targetValue = if (model.isLoading) 0f else targetHeight,
                animationSpec = MaterialTheme.motion.valueChange,
                label = "ColumnChartHeight",
            )

            val toneColor = animateColorAsState(
                targetValue = column.tone.color(colors = MaterialTheme.colorScheme),
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "ColumnChartTone",
            )

            val dimmed = activeIndex >= 0 && index != activeIndex
            val barAlpha = animateFloatAsState(
                targetValue = if (dimmed) 0.35f else 1f,
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "ColumnChartDim",
            )

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
                            FadeThroughText(
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
                            .layout { measurable, constraints ->
                                val barHeight = (if (model.isLoading) targetHeight else grownHeight.value).dp
                                    .roundToPx()
                                    .coerceIn(
                                        minimumValue = constraints.minHeight,
                                        maximumValue = constraints.maxHeight,
                                    )

                                val placeable = measurable.measure(
                                    constraints.copy(
                                        minHeight = barHeight,
                                        maxHeight = barHeight,
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
                            .drawBehind {
                                drawRect(
                                    color = toneColor.value,
                                    alpha = barAlpha.value,
                                )
                            }
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
