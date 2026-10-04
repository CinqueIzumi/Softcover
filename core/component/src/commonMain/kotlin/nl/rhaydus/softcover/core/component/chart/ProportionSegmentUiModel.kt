package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable

/** One slice of a proportion chart: its stack width as [fraction] and its legend text as [label] and [share]. */
@Immutable
data class ProportionSegmentUiModel(
    val label: String,
    val share: String,
    val fraction: Float,
    val tone: ChartTone,
)
