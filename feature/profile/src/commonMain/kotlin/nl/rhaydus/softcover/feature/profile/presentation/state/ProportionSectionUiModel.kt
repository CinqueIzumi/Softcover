package nl.rhaydus.softcover.feature.profile.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel

@Immutable
internal data class ProportionSectionUiModel(
    val title: String,
    val caption: String,
    val emptyCaption: String,
    val chart: ProportionChartUiModel,
    val hasData: Boolean,
)
