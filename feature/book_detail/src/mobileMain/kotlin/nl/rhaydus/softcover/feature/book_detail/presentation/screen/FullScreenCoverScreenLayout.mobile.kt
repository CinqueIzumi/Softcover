package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import nl.rhaydus.softcover.core.component.cover.CoverUiModel

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
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { zoomState.toggleDoubleTapZoom() },
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomState.zoomBy(
                        factor = zoom,
                        pan = pan,
                    )
                }
            },
    )
}
