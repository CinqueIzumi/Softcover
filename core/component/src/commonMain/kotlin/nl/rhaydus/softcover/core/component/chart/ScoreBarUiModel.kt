package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable

@Immutable
data class ScoreBarUiModel(
    val label: String,
    val valueText: String,
    val fill: Float,
    val valueContentDescription: String? = null,
)
