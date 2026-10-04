package nl.rhaydus.softcover.core.component.chart

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The fill of a chart bar or legend swatch. `Lead` to `Fifth` step the primary colour down in emphasis;
 * `Unknown` is a muted neutral, so a bucket nobody could classify never reads as data.
 */
enum class ChartTone {
    Lead,
    Second,
    Third,
    Fourth,
    Fifth,
    Unknown,
    ;

    companion object {
        /** The tone for a zero-based rank; ranks past `Fifth` keep its fill. */
        fun forRank(rank: Int): ChartTone = entries[rank.coerceIn(
            0,
            Fifth.ordinal,
        )]
    }
}

@Composable
internal fun ChartTone.color(): Color {
    val colors = MaterialTheme.colorScheme

    return when (this) {
        ChartTone.Lead -> colors.primary
        ChartTone.Second -> colors.primary.copy(alpha = 0.82f)
        ChartTone.Third -> colors.primary.copy(alpha = 0.64f)
        ChartTone.Fourth -> colors.primary.copy(alpha = 0.48f)
        ChartTone.Fifth -> colors.primary.copy(alpha = 0.34f)
        ChartTone.Unknown -> colors.onSurfaceVariant.copy(alpha = 0.22f)
    }
}
