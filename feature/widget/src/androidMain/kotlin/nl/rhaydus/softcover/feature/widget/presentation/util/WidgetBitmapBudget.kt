package nl.rhaydus.softcover.feature.widget.presentation.util

import android.content.res.Resources
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Bytes in one ARGB_8888 pixel. */
internal const val BYTES_PER_PIXEL = 4

private const val REMOTE_VIEWS_CAP_FACTOR = 1.5f
private const val BUDGET_SHARE = 0.5f
private const val COVER_ASPECT = 2f / 3f

/** The most bitmap bytes Android accepts in one RemoteViews update: 1.5 screens of ARGB_8888 pixels. */
internal fun remoteViewsBitmapCapBytes(): Long {
    val screen = Resources.getSystem().displayMetrics

    return (REMOTE_VIEWS_CAP_FACTOR * screen.widthPixels * screen.heightPixels * BYTES_PER_PIXEL).toLong()
}

/**
 * Glance composes once per reported size and each composition carries its own copy of every bitmap, so an update
 * weighs [sizeCount] times what one size holds. Returns [requestedPx] while that stays within half of [capBytes],
 * else the tallest cover height that does, never below 1. Each size holds [coversPerSize] covers of [aspect]
 * (width over height) plus [fixedBytesPerSize] of images that do not scale with the cover.
 *
 * Covers are assumed 2:3. A square cover costs about 1.5 times as much and still fits inside the margin left by
 * budgeting only half of [capBytes].
 */
internal fun allowedCoverPx(
    capBytes: Long,
    sizeCount: Int,
    fixedBytesPerSize: Long,
    coversPerSize: Int,
    requestedPx: Int,
    aspect: Float = COVER_ASPECT,
): Int {
    val budgetPerSize = capBytes * BUDGET_SHARE / sizeCount.coerceAtLeast(1)
    val coverBytes = (budgetPerSize - fixedBytesPerSize) / coversPerSize.coerceAtLeast(1)
    val fittingPx = floor(sqrt(coverBytes / (BYTES_PER_PIXEL * aspect))).toInt()

    return requestedPx.coerceAtMost(fittingPx).coerceAtLeast(1)
}

/**
 * [allowedCoverPx] for a cover height in dp against the current screen's cap. [requestedDp] comes back untouched
 * when it fits, so a widget under budget keeps its render size and cache keys.
 */
internal fun fittingCoverHeightDp(
    requestedDp: Int,
    density: Float,
    sizeCount: Int,
    fixedBytesPerSize: Long,
    coversPerSize: Int,
): Int {
    val requestedPx = (requestedDp * density).roundToInt()

    val allowedPx = allowedCoverPx(
        capBytes = remoteViewsBitmapCapBytes(),
        sizeCount = sizeCount,
        fixedBytesPerSize = fixedBytesPerSize,
        coversPerSize = coversPerSize,
        requestedPx = requestedPx,
    )

    if (allowedPx >= requestedPx) return requestedDp

    return floor(allowedPx / density).toInt().coerceAtLeast(1)
}
