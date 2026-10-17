package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.yearMetricToggleFor
import nl.rhaydus.toad.ActionScope

internal class YearMetricToggleCollector : ProfileCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
        dependencies: ProfileDependencies,
    ) {
        scope.state
            .map { it.selectedYearMetric to (it.isLoading.not() && it.readingLife != null) }
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
