package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.YearMetric
import nl.rhaydus.toad.ActionScope

/** Picks which series the year-column chart reads; `YearMetricToggleCollector` rebuilds the toggle from it. */
internal class OnYearMetricSelectedAction(val metric: YearMetric) : ProfileAction {
    override suspend fun execute(
        dependencies: ProfileDependencies,
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
    ) {
        scope.setState { it.copy(selectedYearMetric = metric) }
    }
}
