package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.share.CapturableShareCard
import kotlin.math.roundToInt

/**
 * Shows a share card shrunk to fit [maxWidth] by [maxHeight], centred, never enlarged past its natural size.
 *
 * The card is laid out at its natural size and only drawn scaled, so [capture] still records the full-size
 * card.
 */
@Composable
fun ScaledShareCardPreview(
    capture: ShareCardCapture,
    modifier: Modifier = Modifier,
    maxWidth: Dp = 280.dp,
    maxHeight: Dp = 360.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center,
    ) {
        CapturableShareCard(
            capture = capture.foundation,
            modifier = Modifier.layout { measurable, _ ->
                val placeable = measurable.measure(constraints = Constraints())

                val scale = minOf(
                    maxWidth.toPx() / placeable.width,
                    maxHeight.toPx() / placeable.height,
                    1f,
                )

                val scaledWidth = (placeable.width * scale).roundToInt()
                val scaledHeight = (placeable.height * scale).roundToInt()

                layout(width = scaledWidth, height = scaledHeight) {
                    placeable.placeWithLayer(
                        x = -(placeable.width - scaledWidth) / 2,
                        y = -(placeable.height - scaledHeight) / 2,
                    ) {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin.Center
                    }
                }
            },
        ) {
            content()
        }
    }
}
