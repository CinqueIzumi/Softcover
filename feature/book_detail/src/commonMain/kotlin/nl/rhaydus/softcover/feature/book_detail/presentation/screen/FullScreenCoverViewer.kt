package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import nl.rhaydus.designsystem.image.RhaydusShimmerImage
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.chrome.overMediaIconButtonColors
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.rememberCoverImageRequest
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

/**
 * The full-screen cover viewer chrome shared by both platforms: a black surface, the cover scaled and
 * translated by the caller's [scale] / [offset] (the `graphicsLayer`), and a close affordance. The
 * caller owns the zoom/pan state and supplies the platform-specific [imageGestureModifier] (touch
 * pinch + double-tap on mobile; mouse wheel + drag + double-click on desktop) and the [onSizeChanged]
 * sink for the container bounds used by [clampCoverOffset]. Hover/cursor on the close button is inert
 * on touch, so mobile renders identically.
 */
@Composable
internal fun FullScreenCoverViewer(
    model: CoverUiModel,
    scale: Float,
    offset: Offset,
    onSizeChanged: (IntSize) -> Unit,
    onNavigateUp: () -> Unit,
    imageGestureModifier: Modifier,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
            .onSizeChanged(onSizeChanged),
    ) {
        RhaydusShimmerImage(
            model = rememberCoverImageRequest(model = model),
            contentDescription = "Full screen book cover",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .then(imageGestureModifier),
        )

        ChromeIconButton(
            icon = SoftcoverIcon.Close,
            contentDescription = "Close cover viewer",
            tooltip = "Close",
            onClick = onNavigateUp,
            colors = overMediaIconButtonColors(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(Spacing.xs),
        )
    }
}
