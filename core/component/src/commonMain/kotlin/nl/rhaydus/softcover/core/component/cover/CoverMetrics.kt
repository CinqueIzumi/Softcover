package nl.rhaydus.softcover.core.component.cover

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

internal data class CoverMetrics(
    val elevation: Dp,
    val cornerRadius: Dp,
    val shadowColor: Color = Color.Unspecified,
    val maxDecodePx: Int? = null,
)
