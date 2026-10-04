package nl.rhaydus.softcover.core.uibinding.format

import kotlin.math.roundToInt

/** [fraction], in `0..1`, as a whole percent such as "42%". */
fun percentLabel(fraction: Float): String {
    val clamped = fraction.coerceIn(
        minimumValue = 0f,
        maximumValue = 1f,
    )

    return "${(clamped * 100).roundToInt()}%"
}
