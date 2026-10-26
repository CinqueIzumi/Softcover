package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.readingSummaryUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope

internal class ReadingSummaryCollector : ProfileCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
        dependencies: ProfileDependencies,
    ) {
        dependencies.observeReadingHistoryUseCase()
            .untilBackfilled(dependencies.observeReadingStatsBackfilledUseCase)
            .filterNotNull()
            .collect { history ->
                val currentYear = dependencies.clock
                    .now()
                    .toLocalDateTime(dependencies.timeZone)
                    .year

                scope.setState {
                    it.copy(
                        readingSummary = readingSummaryUiModel(
                            history = history,
                            currentYear = currentYear,
                        ),
                    )
                }
            }
    }
}
