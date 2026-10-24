package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.YearMetric
import nl.rhaydus.toad.ActionScope

internal class YearHistoryCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        scope.state
            .map { state ->
                val readingLife = state.readingLife

                val series: List<YearCount>? = when {
                    state.isLoading || readingLife == null -> null
                    state.selectedYearMetric == YearMetric.BOOKS -> readingLife.booksByYear
                    else -> readingLife.pagesByYear
                }

                series to state.selectedYearMetric
            }
            .distinctUntilChanged()
            .collectLatest { (series, metric) ->
                scope.setState {
                    it.copy(
                        yearHistoryChart = yearHistoryChartUiModel(
                            series = series,
                            metric = metric,
                        ),
                        yearHistoryCaption = yearHistoryCaption(
                            series = series,
                            metric = metric,
                        ),
                    )
                }
            }
    }
}
