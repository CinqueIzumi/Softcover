package nl.rhaydus.softcover.core.component.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import kotlin.math.roundToInt
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

private const val DAY_COLUMNS = 31

@Composable
fun YearGrid(
    model: YearGridUiModel,
    onEvent: (YearGridEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labelStyle = MaterialTheme.typography.labelSmall
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val currentOnEvent by rememberUpdatedState(onEvent)
    val colors = MaterialTheme.colorScheme
    val emptyColor = colors.surfaceContainerHigh
    val stepColors = remember(colors) { YearGridStep.entries.map { it.tone().color(colors) } }
    var hovered by remember(model.months, model.isLoading) { mutableStateOf<IntOffset?>(null) }
    val gap = Spacing.xxxs

    val labelWidth = remember(model.months, labelStyle, density) {
        val widest = model.months.maxOfOrNull { month ->
            textMeasurer
                .measure(
                    text = month.label,
                    style = labelStyle,
                )
                .size
                .width
        } ?: 0

        with(density) { widest.toDp() }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cell = ((maxWidth - labelWidth - Spacing.xs - gap * (DAY_COLUMNS - 1)) / DAY_COLUMNS)
            .coerceIn(
                minimumValue = 0.dp,
                maximumValue = 14.dp,
            )
        val rows = model.months.size.coerceAtLeast(1)
        val cellPx = with(density) { cell.toPx() }
        val gapPx = with(density) { gap.toPx() }
        val pitchPx = cellPx + gapPx
        val gridWidthPx = cellPx * DAY_COLUMNS + gapPx * (DAY_COLUMNS - 1)

        fun left(column: Int): Float = cellLeft(
            column = column,
            pitchPx = pitchPx,
            cellPx = cellPx,
            gridWidthPx = gridWidthPx,
            isRtl = isRtl,
        )

        fun hit(position: Offset): IntOffset? = readCellAt(
            position = position,
            model = model,
            pitchPx = pitchPx,
            gridWidthPx = gridWidthPx,
            isRtl = isRtl,
        )

        val hoveredCell = hovered?.let { model.months.getOrNull(it.y)?.days?.getOrNull(it.x) } as? YearGridCell.Read

        Column(
            modifier = Modifier.width(labelWidth + Spacing.xs + cell * DAY_COLUMNS + gap * (DAY_COLUMNS - 1)),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row {
                Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                    model.months.forEach { month ->
                        Box(
                            modifier = Modifier
                                .width(labelWidth)
                                .height(cell),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            SoftcoverText(
                                text = month.label,
                                modifier = Modifier.wrapContentHeight(
                                    align = Alignment.CenterVertically,
                                    unbounded = true,
                                ),
                                style = labelStyle,
                                color = colors.onSurfaceVariant,
                                isLoading = model.isLoading,
                                standIn = "Mmm",
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.xs))

                Box(
                    modifier = Modifier
                        .size(
                            width = cell * DAY_COLUMNS + gap * (DAY_COLUMNS - 1),
                            height = cell * rows + gap * (rows - 1),
                        )
                        .drawBehind {
                            val radius = CornerRadius(2.dp.toPx())

                            model.months.forEachIndexed { row, month ->
                                month.days.forEachIndexed { column, day ->
                                    drawRoundRect(
                                        color = when (day) {
                                            YearGridCell.Empty -> emptyColor
                                            is YearGridCell.Read -> stepColors[day.step.ordinal]
                                        },
                                        topLeft = Offset(
                                            x = left(column),
                                            y = row * pitchPx,
                                        ),
                                        size = Size(
                                            width = cellPx,
                                            height = cellPx,
                                        ),
                                        cornerRadius = radius,
                                    )
                                }
                            }
                        }
                        .skeletonMask(
                            visible = model.isLoading,
                            shape = Corners.xxs,
                        )
                        .then(
                            if (model.isLoading) {
                                Modifier
                            } else {
                                Modifier
                                    .then(if (hovered != null) Modifier.pointerHandCursor() else Modifier)
                                    .pointerInput(model.months, pitchPx, cellPx, isRtl) {
                                        detectTapGestures { position ->
                                            val hit = hit(position)
                                            val day = hit?.let { model.months[it.y].days[it.x] } as? YearGridCell.Read

                                            day?.let { currentOnEvent(YearGridEvent.DayTapped(it.date)) }
                                        }
                                    }
                                    .pointerInput(model.months, pitchPx, cellPx, isRtl) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                val change = event.changes.firstOrNull()

                                                if (change != null && change.type == PointerType.Mouse) {
                                                    when (event.type) {
                                                        PointerEventType.Enter,
                                                        PointerEventType.Move,
                                                        -> hovered = hit(change.position)

                                                        PointerEventType.Exit -> hovered = null
                                                    }
                                                }
                                            }
                                        }
                                    }
                            },
                        ),
                ) {
                    if (model.isLoading.not()) {
                        model.months.forEachIndexed { row, month ->
                            month.days.forEachIndexed { column, day ->
                                if (day is YearGridCell.Read) {
                                    Box(
                                        modifier = Modifier
                                            .offset {
                                                IntOffset(
                                                    x = (left(column) - if (isRtl) gapPx else 0f).roundToInt(),
                                                    y = (row * pitchPx).roundToInt(),
                                                )
                                            }
                                            .size(cell + gap)
                                            .semantics {
                                                role = Role.Button
                                                contentDescription = day.contentDescription
                                                onClick {
                                                    currentOnEvent(YearGridEvent.DayTapped(day.date))
                                                    true
                                                }
                                            },
                                    )
                                }
                            }
                        }
                    }

                    ColumnReadout(
                        text = hoveredCell?.contentDescription,
                        centerX = hovered?.let { left(it.x) + cellPx / 2f },
                        rowWidth = gridWidthPx,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = Spacing.xxs,
                    alignment = Alignment.End,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SoftcoverText(
                    text = model.fewerLabel,
                    style = labelStyle,
                    color = colors.onSurfaceVariant,
                    isLoading = model.isLoading,
                    standIn = "Fewer pages",
                    maxLines = 1,
                )

                Row(
                    modifier = Modifier.skeletonMask(
                        visible = model.isLoading,
                        shape = Corners.xxs,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    (listOf(emptyColor) + stepColors).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(cell)
                                .background(
                                    color = color,
                                    shape = Corners.xxs,
                                ),
                        )
                    }
                }

                SoftcoverText(
                    text = model.moreLabel,
                    style = labelStyle,
                    color = colors.onSurfaceVariant,
                    isLoading = model.isLoading,
                    standIn = "More",
                    maxLines = 1,
                )
            }
        }
    }
}

internal fun cellLeft(
    column: Int,
    pitchPx: Float,
    cellPx: Float,
    gridWidthPx: Float,
    isRtl: Boolean,
): Float = if (isRtl) gridWidthPx - column * pitchPx - cellPx else column * pitchPx

/** The (column, row) of the read day under [position], counting the gap beside a cell as part of it. */
internal fun readCellAt(
    position: Offset,
    model: YearGridUiModel,
    pitchPx: Float,
    gridWidthPx: Float,
    isRtl: Boolean,
): IntOffset? {
    val x = if (isRtl) gridWidthPx - position.x else position.x
    val column = floor(x / pitchPx).toInt()
    val row = floor(position.y / pitchPx).toInt()
    val day = model.months.getOrNull(row)?.days?.getOrNull(column)

    val isReadDay = day is YearGridCell.Read && x >= 0f && position.y >= 0f

    return if (isReadDay) {
        IntOffset(
            x = column,
            y = row,
        )
    } else {
        null
    }
}
