package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.YearMetric
import nl.rhaydus.toad.ActionScope

/** Picks which series the year-column chart reads; `YearMetricToggleCollector` rebuilds the toggle from it. */
internal class OnYearMetricSelectedAction(val metric: YearMetric) : StatsAtlasAction {
    override suspend fun execute(
        dependencies: StatsAtlasDependencies,
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
    ) {
        scope.setState { it.copy(selectedYearMetric = metric) }
    }
}
