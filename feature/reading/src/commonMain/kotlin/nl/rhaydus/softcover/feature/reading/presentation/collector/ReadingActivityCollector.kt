package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.core.presentation.error.onApiFailure
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
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
                                recentReadingActivity = activity,
                                streakStripHeader = streakStripHeader(activity = activity),
                            )
                        }
                    }
            } else {
                scope.setState {
                    it.copy(
                        recentReadingActivity = emptyList(),
                        streakStripHeader = streakStripHeader(activity = emptyList()),
                    )
                }
            }
        }
    }
}

private fun streakStripHeader(activity: List<ReadingDayActivity>): SectionHeaderUiModel {
    val readDays = activity.count { it.didRead }

    val headline = when (readDays) {
        0 -> "No reading logged yet"
        1 -> "You read on 1 of the last ${activity.size} days"
        else -> "You read on $readDays of the last ${activity.size} days"
    }

    return SectionHeaderUiModel.Section(
        eyebrow = "Last ${activity.size} days",
        headline = headline,
    )
}
