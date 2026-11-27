package nl.rhaydus.softcover.feature.profile.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel

@Immutable
internal data class ColumnSectionUiModel(
    val title: String,
    val caption: String,
    val emptyCaption: String,
    val chart: ColumnChartUiModel,
    val hasData: Boolean,
)
