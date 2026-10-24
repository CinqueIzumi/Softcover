package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.yearMetricToggleFor
import nl.rhaydus.toad.ActionScope

internal class YearMetricToggleCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        scope.state
            .map { it.selectedYearMetric to (it.isLoading.not() && it.readingHistory != null) }
            .distinctUntilChanged()
            .collectLatest { (metric, ready) ->
                val toggle = yearMetricToggleFor(
                    selected = metric,
                    enabled = ready,
                )

                scope.setState { it.copy(yearMetricToggle = toggle) }
            }
    }
}
