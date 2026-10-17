package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.IntState
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

internal class BannerCoveredInsets(
    private val coveredHeight: IntState,
    private val statusBars: WindowInsets,
) : WindowInsets {
    override fun getTop(density: Density): Int = minOf(
        coveredHeight.intValue,
        statusBars.getTop(density),
    )

    override fun getBottom(density: Density): Int = 0

    override fun getLeft(
        density: Density,
        layoutDirection: LayoutDirection,
    ): Int = 0

    override fun getRight(
        density: Density,
        layoutDirection: LayoutDirection,
    ): Int = 0
}
