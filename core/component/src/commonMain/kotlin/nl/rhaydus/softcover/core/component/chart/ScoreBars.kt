package nl.rhaydus.softcover.core.component.chart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.designsystem.modifier.skeletonMask
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.designsystem.theme.Corners
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.designsystem.theme.motion
import nl.rhaydus.softcover.core.component.text.FadeThroughText
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.component.transition.ExitingItems
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.tabularFigures
import kotlin.math.roundToInt

@Composable
fun ScoreBars(
    model: ScoreBarsUiModel,
    modifier: Modifier = Modifier,
) {
    val playMotion = playDecorativeMotion()
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    val items = remember(model.rows, model.isLoading) {
        model.rows.mapIndexed { index, row ->
            ScoreBarItem(
                index = index,
                row = row,
                isLoading = model.isLoading,
            )
        }.toImmutableList()
    }

    val followsLoading = remember { mutableStateOf(false) }
    val revealsInPlace = followsLoading.value && model.isLoading.not()

    SideEffect { followsLoading.value = model.isLoading }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        ExitingItems(
            items = items,
            keyOf = { if (it.isLoading) it.index else it.row.label },
            animatesExit = { it.isLoading.not() },
            exit = if (playMotion) {
                shrinkVertically(
                    animationSpec = spatialSpec,
                    shrinkTowards = Alignment.Top,
                ) + fadeOut(animationSpec = effectsSpec)
            } else {
                ExitTransition.None
            },
        ) { item, _ ->
            val enterState = remember { MutableTransitionState(playMotion.not() || item.isLoading || revealsInPlace) }

            enterState.targetState = true

            AnimatedVisibility(
                visibleState = enterState,
                enter = expandVertically(
                    animationSpec = spatialSpec,
                    expandFrom = Alignment.Top,
                ) + fadeIn(animationSpec = effectsSpec),
                exit = ExitTransition.None,
            ) {
                ScoreBarRow(
                    row = item.row,
                    isLoading = item.isLoading,
                )
            }
        }
    }
}

private data class ScoreBarItem(
    val index: Int,
    val row: ScoreBarUiModel,
    val isLoading: Boolean,
)

@Composable
private fun ScoreBarRow(
    row: ScoreBarUiModel,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = Corners.xxs

    val fill by animateFloatAsState(
        targetValue = if (isLoading) 0f else row.fill.coerceIn(0f..1f),
        animationSpec = MaterialTheme.motion.valueChange,
        label = "ScoreBarsFill",
    )

    Column(
        modifier = modifier.then(
            if (isLoading) {
                Modifier
            } else {
                Modifier.clearAndSetSemantics {
                    contentDescription = "${row.label}, ${row.valueContentDescription ?: row.valueText}"
                }
            },
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.Bottom,
        ) {
            SoftcoverText(
                text = row.label,
                style = MaterialTheme.editorialTypography.titleSmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurface,
                isLoading = isLoading,
                standIn = "Label name",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            FadeThroughText(
                text = row.valueText,
                style = MaterialTheme.typography.labelMedium.tabularFigures(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                isLoading = isLoading,
                standIn = "0.0",
                maxLines = 1,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
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
                        val fillWidth = (constraints.maxWidth * fill).roundToInt()

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
}
