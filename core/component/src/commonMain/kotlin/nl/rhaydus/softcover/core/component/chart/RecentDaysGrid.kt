package nl.rhaydus.softcover.core.component.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

private const val ROWS = 7
private const val COLUMNS = 12

@Composable
fun RecentDaysGrid(
    model: RecentDaysGridUiModel,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val emptyColor = colors.surfaceContainerHigh
    val stepColors = remember(colors) { YearGridStep.entries.map { it.tone().color(colors) } }
    val cell = 12.dp
    val gap = Spacing.xxxs

    Box(
        modifier = modifier
            .size(
                width = cell * COLUMNS + gap * (COLUMNS - 1),
                height = cell * ROWS + gap * (ROWS - 1),
            )
            .semantics {
                role = Role.Image
                contentDescription = model.contentDescription
            }
            .drawBehind {
                val cellPx = cell.toPx()
                val pitchPx = cellPx + gap.toPx()
                val radius = CornerRadius(3.dp.toPx())

                model.days.forEachIndexed { index, step ->
                    drawRoundRect(
                        color = step?.let { stepColors[it.ordinal] } ?: emptyColor,
                        topLeft = Offset(
                            x = (index / ROWS) * pitchPx,
                            y = (index % ROWS) * pitchPx,
                        ),
                        size = Size(
                            width = cellPx,
                            height = cellPx,
                        ),
                        cornerRadius = radius,
                    )
                }
            }
            .skeletonMask(
                visible = model.isLoading,
                shape = Corners.xxs,
            ),
    )
}
