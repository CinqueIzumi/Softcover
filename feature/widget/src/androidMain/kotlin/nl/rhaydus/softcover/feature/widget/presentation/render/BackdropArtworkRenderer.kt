package nl.rhaydus.softcover.feature.widget.presentation.render

import android.graphics.Bitmap
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt

/** Renders for one widget instance and keeps the backdrops of the last cover it was given. */
internal class BackdropArtworkRenderer(private val density: Float) {
    /** Backdrops are blurred, so half resolution is invisible once Crop scales them up, and a quarter of the bytes. */
    private val renderDensity = density * RENDER_SCALE

    private val gradients = renderGradientMasks()

    private var lastBackdrops: Triple<Bitmap, Bitmap, Bitmap>? = null

    suspend fun render(cover: Bitmap?): BackdropArtwork {
        val backdrops = cover?.let { backdropsFor(it) }

        return BackdropArtwork(
            compactBackdrop = backdrops?.second,
            tallBackdrop = backdrops?.third,
            gradients = gradients,
        )
    }

    /** The cache is written only once both renders finished, so a cancelled render never replaces it. */
    private suspend fun backdropsFor(cover: Bitmap): Triple<Bitmap, Bitmap, Bitmap> {
        lastBackdrops?.takeIf { it.first === cover }?.let { return it }

        val compact = backdrop(
            cover = cover,
            heightDp = COMPACT_HEIGHT_DP,
        )

        val tall = backdrop(
            cover = cover,
            heightDp = TALL_HEIGHT_DP,
        )

        coroutineContext.ensureActive()

        return Triple(
            cover,
            compact,
            tall,
        ).also { lastBackdrops = it }
    }

    private suspend fun backdrop(
        cover: Bitmap,
        heightDp: Int,
    ): Bitmap = renderBackdrop(
        cover = cover,
        widthPx = (WIDTH_DP * renderDensity).roundToInt(),
        heightPx = (heightDp * renderDensity).roundToInt(),
        sigmaPx = SKIA_SIGMA_PER_RADIUS * BLUR_DP * renderDensity + SKIA_SIGMA_OFFSET,
    )

    private companion object {
        const val RENDER_SCALE = 0.5f
        const val WIDTH_DP = 110
        const val COMPACT_HEIGHT_DP = 110
        const val TALL_HEIGHT_DP = 180

        /** The radius `Modifier.blur` would take; tuned lighter than the Reading hero's 6dp for the widget. */
        const val BLUR_DP = 2f

        /** Skia's blur radius to sigma conversion, which `RenderEffect.createBlurEffect` goes through. */
        const val SKIA_SIGMA_PER_RADIUS = 0.57735f
        const val SKIA_SIGMA_OFFSET = 0.5f
    }
}
