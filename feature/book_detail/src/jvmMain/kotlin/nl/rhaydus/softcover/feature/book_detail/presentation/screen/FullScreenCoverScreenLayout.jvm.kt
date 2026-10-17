package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import nl.rhaydus.designsystem.modifier.dismissOnEscape
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.cover.CoverUiModel

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
    model: CoverUiModel,
    onNavigateUp: () -> Unit,
) {
    val zoomState = rememberCoverZoomState()

    FullScreenCoverViewer(
        model = model,
        scale = zoomState.scale,
        offset = zoomState.offset,
        onSizeChanged = { zoomState.containerSize = it },
        onNavigateUp = onNavigateUp,
        imageGestureModifier = Modifier
            .dismissOnEscape(onDismiss = onNavigateUp)
            .pointerHandCursor()
            .onPointerEvent(PointerEventType.Scroll) { event ->
                val scrollY = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f

                if (scrollY == 0f) return@onPointerEvent

                zoomState.zoomBy(factor = if (scrollY < 0f) WHEEL_ZOOM_STEP else 1f / WHEEL_ZOOM_STEP)
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()

                    zoomState.zoomBy(
                        factor = 1f,
                        pan = dragAmount,
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { zoomState.toggleDoubleTapZoom() },
                )
            },
    )
}
