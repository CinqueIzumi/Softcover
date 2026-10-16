package nl.rhaydus.softcover.feature.widget.presentation.render

import android.graphics.Bitmap
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt

/** Renders for one widget instance and keeps the backdrop of the last cover it was given. */
internal class BackdropArtworkRenderer(private val density: Float) {
    /** Backdrops are blurred, so half resolution is invisible once Crop scales them up, and a quarter of the bytes. */
    private val renderDensity = density * RENDER_SCALE

    private val gradients = renderGradientMasks()

    private var lastBackdrop: Pair<Bitmap, Bitmap>? = null

    suspend fun render(cover: Bitmap?): BackdropArtwork = BackdropArtwork(
        backdrop = cover?.let { backdropFor(it) },
        gradients = gradients,
    )

    private suspend fun backdropFor(cover: Bitmap): Bitmap {
        lastBackdrop?.takeIf { it.first === cover }?.let { return it.second }

        val backdrop = renderBackdrop(
            cover = cover,
            widthPx = (WIDTH_DP * renderDensity).roundToInt(),
            heightPx = (HEIGHT_DP * renderDensity).roundToInt(),
            sigmaPx = SKIA_SIGMA_PER_RADIUS * BLUR_DP * renderDensity + SKIA_SIGMA_OFFSET,
        )

        coroutineContext.ensureActive()

        lastBackdrop = cover to backdrop

        return backdrop
    }

    private companion object {
        const val RENDER_SCALE = 0.5f
        const val WIDTH_DP = 110
        const val HEIGHT_DP = 180

        /** The radius `Modifier.blur` would take; tuned lighter than the Reading hero's 6dp for the widget. */
        const val BLUR_DP = 2f

        /** Skia's blur radius to sigma conversion, which `RenderEffect.createBlurEffect` goes through. */
        const val SKIA_SIGMA_PER_RADIUS = 0.57735f
        const val SKIA_SIGMA_OFFSET = 0.5f
    }
}
