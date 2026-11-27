package nl.rhaydus.softcover.feature.profile.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel

@Immutable
internal data class DemographicBreakdownUiModel(
    val label: String,
    val chart: ProportionChartUiModel,
    val caption: String,
    val hasData: Boolean,
)
