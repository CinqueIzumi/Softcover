package nl.rhaydus.softcover.feature.profile.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel

/** One author breakdown (gender, BIPOC or LGBTQ+); [hasData] is false when nobody is tagged for it. */
@Immutable
internal data class DemographicBreakdownUiModel(
    val chart: ProportionChartUiModel,
    val caption: String,
    val hasData: Boolean,
)
