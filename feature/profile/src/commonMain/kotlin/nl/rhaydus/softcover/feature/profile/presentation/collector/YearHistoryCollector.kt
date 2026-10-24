package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.YearMetric
import nl.rhaydus.toad.ActionScope

/** The year chart is never scoped: a selected calendar year only lights its column and names it in the caption. */
internal class YearHistoryCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        scope.state
            .map { state ->
                val readingHistory = state.readingHistory

                val series: List<YearCount>? = when {
                    state.isLoading || readingHistory == null -> null
                    state.selectedYearMetric == YearMetric.BOOKS -> readingHistory.booksByYear
                    else -> readingHistory.pagesByYear
                }

                YearHistorySnapshot(
                    series = series,
                    metric = state.selectedYearMetric,
                    litYear = (state.selectedScope as? StatsScope.CalendarYear)?.year,
                )
            }
            .distinctUntilChanged()
            .collectLatest { (series, metric, litYear) ->
                scope.setState {
                    it.copy(
                        yearHistoryChart = yearHistoryChartUiModel(
                            series = series,
                            metric = metric,
                            litYear = litYear,
                        ),
                        yearHistoryCaption = yearHistoryCaption(
                            series = series,
                            metric = metric,
                            litYear = litYear,
                        ),
                    )
                }
            }
    }

    private data class YearHistorySnapshot(
        val series: List<YearCount>?,
        val metric: YearMetric,
        val litYear: Int?,
    )
}
