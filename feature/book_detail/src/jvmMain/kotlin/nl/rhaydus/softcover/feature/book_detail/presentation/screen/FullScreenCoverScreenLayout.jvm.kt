package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import coil3.request.ImageRequest
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.pointerHandCursor

// Each wheel notch multiplies / divides the zoom by this factor (between [MIN_SCALE] and [MAX_SCALE]).
private const val WHEEL_ZOOM_STEP = 1.15f

/**
 * Desktop full-screen cover. Pinch-zoom is touch-only, so the desktop viewer drives the same
 * [FullScreenCoverViewer] with mouse affordances instead: the wheel zooms (scroll up to zoom in),
 * click-drag pans, and a double-click toggles between fit and zoom. The clamp + close chrome are
 * shared with mobile.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun FullScreenCoverScreenLayout(
    request: ImageRequest?,
    onNavigateUp: () -> Unit,
) {
    var scale by remember { mutableFloatStateOf(MIN_SCALE) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    FullScreenCoverViewer(
        request = request,
        scale = scale,
        offset = offset,
        onSizeChanged = { containerSize = it },
        onNavigateUp = onNavigateUp,
        imageGestureModifier = Modifier
            .pointerHandCursor()
            .onPointerEvent(PointerEventType.Scroll) { event ->
                val scrollY = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f

                if (scrollY == 0f) return@onPointerEvent

                val factor = if (scrollY < 0f) WHEEL_ZOOM_STEP else 1f / WHEEL_ZOOM_STEP

                val newScale = (scale * factor).coerceIn(
                    MIN_SCALE,
                    MAX_SCALE,
                )
                scale = newScale

                offset = clampCoverOffset(
                    target = offset,
                    currentScale = newScale,
                    containerSize = containerSize,
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()

                    offset = clampCoverOffset(
                        target = offset + dragAmount,
                        currentScale = scale,
                        containerSize = containerSize,
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > MIN_SCALE) {
                            scale = MIN_SCALE
                            offset = Offset.Zero
                        } else {
                            scale = DOUBLE_TAP_SCALE
                        }
                    },
                )
            },
    )
}
