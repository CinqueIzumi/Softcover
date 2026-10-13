package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.core.presentation.error.onApiFailure
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.mapper.streakSheetFor
import nl.rhaydus.softcover.feature.reading.presentation.mapper.streakStripFor
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal class ReadingActivityCollector : ReadingCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        dependencies.getReadingStreakEnabledAsFlowUseCase().collectLatest { enabled ->
            scope.setState { it.copy(streakEnabled = enabled) }

            if (enabled) {
                dependencies.launch {
                    dependencies.refreshReadingActivityUseCase()
                        .onApiFailure()
                }

                dependencies.observeRecentReadingActivityUseCase()
                    .collectLatest { activity: List<ReadingDayActivity> ->
                        scope.setState {
                            it.copy(
                                hasLoadedReadingActivity = true,
                                streakStrip = streakStripFor(
                                    activity = activity,
                                    isLoading = it.isLoading,
                                ),
                                streakSheet = streakSheetFor(activity = activity),
                            )
                        }
                    }
            } else {
                scope.setState {
                    it.copy(
                        hasLoadedReadingActivity = true,
                        streakStrip = streakStripFor(
                            activity = emptyList(),
                            isLoading = it.isLoading,
                        ),
                        streakSheet = streakSheetFor(activity = emptyList()),
                    )
                }
            }
        }
    }
}
