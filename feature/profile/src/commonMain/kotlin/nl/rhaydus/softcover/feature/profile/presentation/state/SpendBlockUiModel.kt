package nl.rhaydus.softcover.feature.profile.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel

@Immutable
internal data class SpendBlockUiModel(
    val title: String,
    val chart: ProportionChartUiModel,
)
