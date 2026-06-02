package nl.rhaydus.softcover.core.designsystem.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent

@Composable
fun CapturableShareCard(
    capture: ShareCardCapture,
    content: ShareContent,
    modifier: Modifier = Modifier,
) {
    val captureModifier = modifier.drawWithContent { recordAndDraw(capture.graphicsLayer) }

    ShareCard(
        content = content,
        modifier = captureModifier,
    )
}
