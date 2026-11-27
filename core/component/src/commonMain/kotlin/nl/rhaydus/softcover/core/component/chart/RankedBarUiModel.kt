package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable

/**
 * One row of [RankedBarsUiModel]: [key] identifies it, [value] trails the bar, [fill] is the share of the track
 * the bar covers, 1 for the lead row.
 */
@Immutable
data class RankedBarUiModel(
    val key: String,
    val label: String,
    val value: String,
    val fill: Float,
)
