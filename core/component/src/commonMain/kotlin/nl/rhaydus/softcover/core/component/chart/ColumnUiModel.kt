package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable

/**
 * One column. [fraction] is its height against the tallest column, and zero draws a short stub. The
 * `Lead` column's [value] is set bold in the primary colour; a null [label] or [value] leaves its slot
 * empty. A screen reader hears only [contentDescription], never the label or value.
 */
@Immutable
data class ColumnUiModel(
    val fraction: Float,
    val tone: ChartTone,
    val label: String?,
    val value: String?,
    val contentDescription: String,
)
