package nl.rhaydus.softcover.core.component.statistic

import androidx.compose.runtime.Immutable

@Immutable
data class CountRowUiModel(
    val label: String,
    val count: String,
)
