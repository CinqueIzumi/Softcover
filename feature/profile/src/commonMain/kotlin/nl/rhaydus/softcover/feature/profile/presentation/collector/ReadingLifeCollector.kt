package nl.rhaydus.softcover.feature.profile.presentation.collector

import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope

internal class ReadingLifeCollector : ProfileCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
        dependencies: ProfileDependencies,
    ) {
        observeLoadedReadingLife(
            observeReadingStatsUseCase = dependencies.observeReadingStatsUseCase,
            observePagesReadUseCase = dependencies.observePagesReadUseCase,
            observeReadingHistoryUseCase = dependencies.observeReadingHistoryUseCase,
            observeReadingStatsBackfilledUseCase = dependencies.observeReadingStatsBackfilledUseCase,
        ).collect { loaded ->
            scope.setState {
                it.copy(
                    readingStats = loaded?.stats,
                    pagesRead = loaded?.pagesRead,
                    readingHistory = loaded?.history,
                )
            }
        }
    }
}
