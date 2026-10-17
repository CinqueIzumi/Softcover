package nl.rhaydus.softcover.feature.widget.presentation.render

import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Blurs opaque [pixels] in place: three box passes per axis approximate a Gaussian of [sigmaPx], and
 * edge pixels are clamped outward so the border neither darkens nor fades.
 */
internal fun blurPixels(
    pixels: IntArray,
    width: Int,
    height: Int,
    sigmaPx: Float,
) {
    if (sigmaPx <= 0f) return

    val scratch = IntArray(pixels.size)

    boxSizes(sigmaPx).forEach { size ->
        val radius = (size - 1) / 2

        boxPass(
            source = pixels,
            target = scratch,
            width = width,
            height = height,
            radius = radius,
            horizontal = true,
        )

        boxPass(
            source = scratch,
            target = pixels,
            width = width,
            height = height,
            radius = radius,
            horizontal = false,
        )
    }
}

/** The odd box widths whose successive passes best match a Gaussian of [sigma]. */
internal fun boxSizes(sigma: Float): List<Int> {
    val ideal = sqrt(12f * sigma * sigma / PASSES + 1f)

    var lower = floor(ideal).toInt()

    if (lower % 2 == 0) lower--

    val upper = lower + 2

    val lowerPasses = ((12f * sigma * sigma - PASSES * lower * lower - 4 * PASSES * lower - 3 * PASSES) /
        (-4 * lower - 4)).roundToInt()

    return List(PASSES) { if (it < lowerPasses) lower else upper }
}

private fun boxPass(
    source: IntArray,
    target: IntArray,
    width: Int,
    height: Int,
    radius: Int,
    horizontal: Boolean,
) {
    val lineCount = if (horizontal) height else width
    val length = if (horizontal) width else height
    val step = if (horizontal) 1 else width
    val window = 2 * radius + 1

    for (line in 0 until lineCount) {
        val start = if (horizontal) line * width else line

        var red = 0
        var green = 0
        var blue = 0

        for (offset in -radius..radius) {
            val pixel = source[start + offset.coerceIn(0..length - 1) * step]

            red += pixel shr RED_SHIFT and CHANNEL
            green += pixel shr GREEN_SHIFT and CHANNEL
            blue += pixel and CHANNEL
        }

        for (position in 0 until length) {
            target[start + position * step] = OPAQUE or
                (red / window shl RED_SHIFT) or
                (green / window shl GREEN_SHIFT) or
                blue / window

            val entering = source[start + (position + radius + 1).coerceIn(0..length - 1) * step]
            val leaving = source[start + (position - radius).coerceIn(0..length - 1) * step]

            red += (entering shr RED_SHIFT and CHANNEL) - (leaving shr RED_SHIFT and CHANNEL)
            green += (entering shr GREEN_SHIFT and CHANNEL) - (leaving shr GREEN_SHIFT and CHANNEL)
            blue += (entering and CHANNEL) - (leaving and CHANNEL)
        }
    }
}

private const val PASSES = 3
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val CHANNEL = 0xFF
private const val OPAQUE = 0xFF shl 24
