package nl.rhaydus.softcover.core.component.chart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.FadeThroughText
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.motion
import nl.rhaydus.softcover.core.designsystem.presentation.theme.tabularFigures

@Composable
fun RankedBars(
    model: RankedBarsUiModel,
    modifier: Modifier = Modifier,
) {
    val playMotion = playDecorativeMotion()
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val positionStyle = MaterialTheme.typography.labelMedium.tabularFigures()
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val windowHeight = LocalWindowInfo.current.containerSize.height.toFloat()
    // Sized for three digits whatever the row count, so expanding never shifts the columns sideways.
    val positionWidth = remember(
        positionStyle,
        density,
    ) {
        with(density) {
            textMeasurer.measure(
                text = "000",
                style = positionStyle,
            ).size.width.toDp()
        }
    }

    Column(modifier = modifier) {
        model.rows.forEachIndexed { index, row ->
            key(if (model.isLoading) index else row.label) {
                val isVisible = index < model.visibleCount

                if (playMotion) {
                    var isOnScreen by remember { mutableStateOf(false) }

                    AnimatedVisibility(
                        visible = isVisible,
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            val bounds = coordinates.boundsInWindow()

                            isOnScreen = bounds.bottom > 0f && bounds.top < windowHeight
                        },
                        enter = expandVertically(
                            animationSpec = spatialSpec,
                            expandFrom = Alignment.Top,
                        ) + fadeIn(animationSpec = effectsSpec),
                        // Rows off screen drop out at once, so a long fold doesn't fling everything below it up.
                        exit = if (isOnScreen) {
                            shrinkVertically(
                                animationSpec = spatialSpec,
                                shrinkTowards = Alignment.Top,
                            )
                        } else {
                            ExitTransition.None
                        },
                    ) {
                        RankedBarRow(
                            row = row,
                            position = index + 1,
                            isLoading = model.isLoading,
                            positionWidth = positionWidth,
                        )
                    }
                } else if (isVisible) {
                    RankedBarRow(
                        row = row,
                        position = index + 1,
                        isLoading = model.isLoading,
                        positionWidth = positionWidth,
                    )
                }
            }
        }
    }
}

@Composable
private fun RankedBarRow(
    row: RankedBarUiModel,
    position: Int,
    isLoading: Boolean,
    positionWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val shape = Corners.xxs
    val positionStyle = MaterialTheme.typography.labelMedium.tabularFigures()
    val fill = animateFloatAsState(
        targetValue = if (isLoading || row.fill <= 0f) 0f else row.fill.coerceIn(0.015f..1f),
        animationSpec = MaterialTheme.motion.valueChange,
        label = "RankedBarsFill",
    )

    Row(
        modifier = modifier.padding(top = if (position > 1) Spacing.sm else 0.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        SoftcoverText(
            text = position.toString(),
            style = positionStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            isLoading = isLoading,
            standIn = "0",
            maxLines = 1,
            modifier = Modifier.width(positionWidth),
        )

        Column(modifier = Modifier.weight(1f)) {
            SoftcoverText(
                text = row.label,
                style = MaterialTheme.editorialTypography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                isLoading = isLoading,
                standIn = "Label name",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .skeletonMask(
                        visible = isLoading,
                        shape = shape,
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .layout { measurable, constraints ->
                            val fillWidth = (constraints.maxWidth * fill.value).roundToInt()

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
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }

        Box(
            modifier = Modifier.width(44.dp),
            contentAlignment = Alignment.TopEnd,
        ) {
            FadeThroughText(
                text = row.share,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                isLoading = isLoading,
                standIn = "00%",
            )
        }
    }
}
