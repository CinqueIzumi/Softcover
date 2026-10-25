package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable

/** One row of [RankedBarsUiModel]: [fill] is the share of the track the bar covers, 1 for the lead row. */
@Immutable
data class RankedBarUiModel(
    val label: String,
    val share: String,
    val fill: Float,
)
