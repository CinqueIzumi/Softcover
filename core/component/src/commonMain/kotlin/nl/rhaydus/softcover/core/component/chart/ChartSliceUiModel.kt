package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable

/**
 * One slice of a chart: [fraction] sizes its bar or stack width, [label] and [share] are its legend text,
 * and [tone] is its fill.
 */
@Immutable
data class ChartSliceUiModel(
    val label: String,
    val share: String,
    val fraction: Float,
    val tone: ChartTone,
)
