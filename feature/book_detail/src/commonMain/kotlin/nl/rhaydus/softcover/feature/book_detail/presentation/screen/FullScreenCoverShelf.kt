package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import nl.rhaydus.designsystem.modifier.pointerHandCursor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.request.ImageRequest
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverImage
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

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
    request: ImageRequest?,
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
        SoftcoverImage(
            model = request,
            contentDescription = "Full screen book cover",
            isLoading = false,
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

        IconButton(
            onClick = onNavigateUp,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.45f),
                contentColor = Color.White,
            ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp)
                .pointerHandCursor(),
        ) {
            val closeIcon = drawableIconResource(
                icon = SoftcoverIcon.Close,
                contentDescription = "Close cover viewer",
            )

            Icon(
                painter = closeIcon.getIconPainter(),
                contentDescription = closeIcon.contentDescription,
            )
        }
    }
}
