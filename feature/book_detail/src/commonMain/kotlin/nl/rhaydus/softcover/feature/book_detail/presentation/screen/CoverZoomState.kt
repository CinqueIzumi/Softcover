package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

internal const val MIN_SCALE = 1f
internal const val MAX_SCALE = 5f
internal const val DOUBLE_TAP_SCALE = 2.5f

/**
 * Clamps a pan [target] so the zoomed cover never reveals empty space past its edges. Returns
 * [Offset.Zero] at (or below) [MIN_SCALE] since an un-zoomed cover cannot pan. Shared by both
 * platforms — the gesture that produces the target differs (touch pinch-pan vs. mouse drag), the
 * clamp does not.
 */
internal fun clampCoverOffset(
    target: Offset,
    currentScale: Float,
    containerSize: IntSize,
): Offset {
    if (currentScale <= MIN_SCALE) return Offset.Zero

    val maxX = (containerSize.width * (currentScale - 1f)) / 2f
    val maxY = (containerSize.height * (currentScale - 1f)) / 2f

    return Offset(
        x = target.x.coerceIn(
            -maxX,
            maxX,
        ),
        y = target.y.coerceIn(
            -maxY,
            maxY,
        ),
    )
}

@Stable
internal class CoverZoomState {
    var scale by mutableFloatStateOf(MIN_SCALE)
        private set

    var offset by mutableStateOf(Offset.Zero)
        private set

    var containerSize by mutableStateOf(IntSize.Zero)

    fun toggleDoubleTapZoom() {
        if (scale > MIN_SCALE) {
            scale = MIN_SCALE
            offset = Offset.Zero
        } else {
            scale = DOUBLE_TAP_SCALE
        }
    }

    fun zoomBy(
        factor: Float,
        pan: Offset = Offset.Zero,
    ) {
        val newScale = (scale * factor).coerceIn(
            MIN_SCALE,
            MAX_SCALE,
        )
        scale = newScale

        offset = clampCoverOffset(
            target = offset + pan,
            currentScale = newScale,
            containerSize = containerSize,
        )
    }
}

@Composable
internal fun rememberCoverZoomState(): CoverZoomState = remember { CoverZoomState() }
