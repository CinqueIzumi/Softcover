package nl.rhaydus.softcover.core.component.chart

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import nl.rhaydus.designsystem.haptics.Haptics

/**
 * Reports the column under a mouse pointer on hover and under a finger during a long-press scrub, as an
 * index into the row, or -1 when none is active. Each column change during a scrub ticks [haptics],
 * including the first. [gapPx] is the column gap in pixels and must match the row's own spacing.
 */
internal fun Modifier.columnChartPointer(
    enabled: Boolean,
    columnCount: Int,
    rowWidth: () -> Float,
    gapPx: Float,
    haptics: Haptics,
    onActiveChange: (Int) -> Unit,
): Modifier = if (enabled) {
    this
        .pointerInput(columnCount, gapPx) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull()

                    if (change != null && change.type == PointerType.Mouse) {
                        when (event.type) {
                            PointerEventType.Enter,
                            PointerEventType.Move,
                            -> onActiveChange(
                                columnIndexAt(
                                    x = change.position.x,
                                    rowWidth = rowWidth(),
                                    count = columnCount,
                                    gap = gapPx,
                                ),
                            )

                            PointerEventType.Exit -> onActiveChange(-1)
                        }
                    }
                }
            }
        }
        .pointerInput(columnCount, gapPx) {
            var current = -1

            fun scrubTo(position: Offset) {
                val index = columnIndexAt(
                    x = position.x,
                    rowWidth = rowWidth(),
                    count = columnCount,
                    gap = gapPx,
                )

                if (index != current) {
                    current = index
                    haptics.tickle()
                    onActiveChange(index)
                }
            }

            fun endScrub() {
                current = -1
                onActiveChange(-1)
            }

            detectDragGesturesAfterLongPress(
                onDragStart = ::scrubTo,
                onDrag = { change, _ -> scrubTo(change.position) },
                onDragEnd = ::endScrub,
                onDragCancel = ::endScrub,
            )
        }
} else {
    this
}
