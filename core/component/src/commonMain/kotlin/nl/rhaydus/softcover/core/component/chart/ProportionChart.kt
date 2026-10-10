package nl.rhaydus.softcover.core.component.chart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.state.clippedSkeletonMask
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Motion
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

@Composable
fun ProportionChart(
    model: ProportionChartUiModel,
    modifier: Modifier = Modifier,
) {
    val barShape = Corners.sm
    val playMotion = playDecorativeMotion()
    val sizeSpec: FiniteAnimationSpec<Float> = if (playMotion) Motion.valueChange else snap()

    val retention = remember {
        SegmentRetention(
            live = model.segments,
            initialLoading = model.isLoading,
        )
    }

    LaunchedEffect(model.segments, model.isLoading) {
        retention.sync(
            live = model.segments,
            isLoading = model.isLoading,
        )
    }

    val revealPercent by animateFloatAsState(
        targetValue = if (model.isLoading) 0f else 100f,
        animationSpec = sizeSpec,
        label = "ProportionChartReveal",
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clippedSkeletonMask(
                    visible = model.isLoading,
                    shape = barShape,
                )
                .drawWithContent {
                    clipRect(right = size.width * revealPercent / 100f) {
                        this@drawWithContent.drawContent()
                    }
                },
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxxs),
        ) {
            retention.entries.forEachIndexed { index, entry ->
                key(if (retention.isLoading) index else entry.slice.label) {
                    BarSegment(
                        entry = entry,
                        sizeSpec = sizeSpec,
                        onGone = { retention.release(entry.slice.label) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        retention.entries.forEachIndexed { index, entry ->
            key(if (retention.isLoading) index else entry.slice.label) {
                val visibleState = remember { MutableTransitionState(entry.phase != SegmentRetention.Phase.Entering) }

                visibleState.targetState = entry.phase != SegmentRetention.Phase.Departing

                val hidden = visibleState.isIdle && visibleState.currentState.not()

                LaunchedEffect(hidden) {
                    if (hidden && entry.phase == SegmentRetention.Phase.Departing && entry.slice.fraction <= 0f) {
                        retention.release(entry.slice.label)
                    }
                }

                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = if (playMotion) {
                        expandVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
                            fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec())
                    } else {
                        EnterTransition.None
                    },
                    exit = if (playMotion) {
                        shrinkVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
                            fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec())
                    } else {
                        ExitTransition.None
                    },
                ) {
                    Column {
                        Divider()

                        ChartLegendRow(
                            slice = entry.slice,
                            isLoading = model.isLoading,
                            swatch = true,
                            modifier = Modifier.padding(vertical = Spacing.xs),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.BarSegment(
    entry: SegmentRetention.Entry,
    sizeSpec: FiniteAnimationSpec<Float>,
    onGone: () -> Unit,
) {
    val departing = entry.phase == SegmentRetention.Phase.Departing
    var armed by remember { mutableStateOf(entry.phase != SegmentRetention.Phase.Entering) }

    LaunchedEffect(Unit) {
        armed = true
    }

    val weightPercent by animateFloatAsState(
        targetValue = if (armed.not() || departing || entry.slice.fraction <= 0f) {
            0f
        } else {
            entry.slice.fraction.coerceAtLeast(0.01f) * 100f
        },
        animationSpec = sizeSpec,
        label = "ProportionChartSegment",
    )

    LaunchedEffect(departing, weightPercent) {
        if (departing && weightPercent == 0f && entry.slice.fraction > 0f) onGone()
    }

    if (weightPercent > 0f) {
        Box(
            modifier = Modifier
                .weight(weightPercent)
                .fillMaxHeight()
                .background(entry.slice.tone.color(colors = MaterialTheme.colorScheme)),
        )
    }
}

