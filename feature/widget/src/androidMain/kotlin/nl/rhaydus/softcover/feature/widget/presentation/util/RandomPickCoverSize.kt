package nl.rhaydus.softcover.feature.widget.presentation.util

import androidx.compose.ui.unit.DpSize

internal const val RANDOM_PICK_EYEBROW_DP = 28

private const val RANDOM_PICK_FOOT_DP = 36
private const val RANDOM_PICK_TALLEST_FOOT_DP = 48

internal fun randomPickEyebrowDp(heightDp: Int): Int = if (heightDp >= TALL_HEIGHT_DP) RANDOM_PICK_EYEBROW_DP else 0

internal fun randomPickFootDp(heightDp: Int): Int =
    if (heightDp >= TALLEST_HEIGHT_DP) RANDOM_PICK_TALLEST_FOOT_DP else RANDOM_PICK_FOOT_DP

internal fun randomPickCoverHeightDp(sizes: List<DpSize>): Int = tallestCoverHeightDp(
    sizes = sizes,
    eyebrowDp = ::randomPickEyebrowDp,
    footDp = ::randomPickFootDp,
)
