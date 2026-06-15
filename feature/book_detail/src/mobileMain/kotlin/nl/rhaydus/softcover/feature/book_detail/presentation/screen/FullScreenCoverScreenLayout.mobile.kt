package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import coil3.request.ImageRequest

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
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(
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
            },
    )
}
