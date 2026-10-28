package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.combine
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
        combine(
            dependencies.observeReadingHistoryUseCase()
                .untilBackfilled(dependencies.observeReadingStatsBackfilledUseCase()),
            dependencies.observeReadingActivityUseCase()
                .untilBackfilled(dependencies.observeReadingStatsBackfilledUseCase()),
            dependencies.observeCurrentDateUseCase(),
        ) { history, activity, today ->
            Triple(
                history,
                activity,
                today,
            )
        }.collect { (history, activity, today) ->
            scope.setState {
                it.copy(
                    readingSummary = readingSummaryUiModel(
                        history = history,
                        activity = activity,
                        today = today,
                    ),
                    currentStreakDays = activity?.currentStreak(today)?.days ?: 0,
                )
            }
        }
    }
}
