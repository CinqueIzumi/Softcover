package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable

/** One ranked bar. [fraction] fills the bar against its own full-width track; [share] is the text beside it. */
@Immutable
data class RankedBarUiModel(
    val label: String,
    val share: String,
    val fraction: Float,
    val tone: ChartTone,
)
