package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
                combine(
                    dependencies.observeReadingActivityUseCase(),
                    dependencies.observeCurrentDateUseCase(),
                ) { activity, today ->
                    activity to today
                }.collectLatest { (activity, today) ->
                    scope.setState {
                        it.copy(
                            hasLoadedReadingActivity = true,
                            streakStrip = streakStripFor(
                                activity = activity,
                                today = today,
                                isLoading = it.isLoading,
                            ),
                            streakSheet = streakSheetFor(
                                activity = activity,
                                today = today,
                            ),
                        )
                    }
                }
            } else {
                scope.setState {
                    it.copy(
                        hasLoadedReadingActivity = true,
                        streakStrip = streakStripFor(
                            activity = null,
                            today = null,
                            isLoading = it.isLoading,
                        ),
                        streakSheet = streakSheetFor(
                            activity = null,
                            today = null,
                        ),
                    )
                }
            }
        }
    }
}
