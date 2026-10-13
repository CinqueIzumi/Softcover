package nl.rhaydus.softcover.core.component.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import nl.rhaydus.designsystem.share.ShareCardCaptureConfig
import nl.rhaydus.designsystem.share.rememberShareCardCapture as rememberFoundationShareCardCapture

@Composable
actual fun rememberShareCardCapture(config: ShareCardCaptureConfig): ShareCardCapture {
    val foundation = rememberFoundationShareCardCapture(config = config)

    return remember(foundation) { DelegatingShareCardCapture(foundation = foundation) }
}
