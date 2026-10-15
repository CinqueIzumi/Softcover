package nl.rhaydus.softcover.feature.widget.backdrop

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt
import kotlinx.coroutines.ensureActive

/**
 * The cover centre-cropped to [widthPx] by [heightPx] and blurred with a Gaussian of [sigmaPx]. Stands in
 * for the Reading hero's `Modifier.blur`, which Glance cannot draw on any API level. Transparent regions
 * of the cover are flattened onto white. Checks for cancellation between stages.
 */
internal suspend fun renderBackdrop(
    cover: Bitmap,
    widthPx: Int,
    heightPx: Int,
    sigmaPx: Float,
): Bitmap {
    val cropped = centerCrop(
        source = cover,
        aspect = widthPx.toFloat() / heightPx,
    )

    coroutineContext.ensureActive()

    val fitted = shrink(
        bitmap = cropped,
        widthPx = widthPx,
        heightPx = heightPx,
    )

    coroutineContext.ensureActive()

    val opaque = Bitmap.createBitmap(
        widthPx,
        heightPx,
        Bitmap.Config.ARGB_8888,
    )

    Canvas(opaque).apply {
        drawColor(Color.WHITE)
        drawBitmap(
            fitted,
            0f,
            0f,
            null,
        )
    }

    val pixels = IntArray(widthPx * heightPx)

    opaque.getPixels(
        pixels,
        0,
        widthPx,
        0,
        0,
        widthPx,
        heightPx,
    )

    coroutineContext.ensureActive()

    blurPixels(
        pixels = pixels,
        width = widthPx,
        height = heightPx,
        sigmaPx = sigmaPx,
    )

    return Bitmap.createBitmap(
        pixels,
        widthPx,
        heightPx,
        Bitmap.Config.ARGB_8888,
    )
}

/**
 * A white alpha ramp down [rows] rows, linear between [stops] of (position 0..1, alpha 0..1), for tinting
 * with a theme colour so a palette or day/night switch needs no re-render.
 */
internal fun renderAlphaGradient(
    stops: List<Pair<Float, Float>>,
    rows: Int,
): Bitmap {
    val pixels = IntArray(GRADIENT_WIDTH * rows) { index ->
        val alpha = alphaAt(
            stops = stops,
            position = (index / GRADIENT_WIDTH).toFloat() / (rows - 1),
        )

        ((alpha * OPAQUE).roundToInt() shl ALPHA_SHIFT) or WHITE
    }

    return Bitmap.createBitmap(
        pixels,
        GRADIENT_WIDTH,
        rows,
        Bitmap.Config.ARGB_8888,
    )
}

internal fun alphaAt(
    stops: List<Pair<Float, Float>>,
    position: Float,
): Float {
    val next = stops.indexOfFirst { it.first >= position }

    if (next < 0) return stops.last().second

    if (next == 0) return stops.first().second

    val (startPosition, startAlpha) = stops[next - 1]
    val (endPosition, endAlpha) = stops[next]

    return startAlpha + (endAlpha - startAlpha) * (position - startPosition) / (endPosition - startPosition)
}

private fun centerCrop(
    source: Bitmap,
    aspect: Float,
): Bitmap {
    val sourceAspect = source.width.toFloat() / source.height

    val width = if (sourceAspect > aspect) (source.height * aspect).roundToInt() else source.width
    val height = if (sourceAspect > aspect) source.height else (source.width / aspect).roundToInt()

    return Bitmap.createBitmap(
        source,
        (source.width - width) / 2,
        (source.height - height) / 2,
        width,
        height,
    )
}

private fun shrink(
    bitmap: Bitmap,
    widthPx: Int,
    heightPx: Int,
): Bitmap {
    var current = bitmap

    while (current.width / 2 >= widthPx && current.height / 2 >= heightPx) {
        current = Bitmap.createScaledBitmap(
            current,
            current.width / 2,
            current.height / 2,
            true,
        )
    }

    return Bitmap.createScaledBitmap(
        current,
        widthPx,
        heightPx,
        true,
    )
}

private const val GRADIENT_WIDTH = 8
private const val OPAQUE = 255
private const val ALPHA_SHIFT = 24
private const val WHITE = 0xFFFFFF
