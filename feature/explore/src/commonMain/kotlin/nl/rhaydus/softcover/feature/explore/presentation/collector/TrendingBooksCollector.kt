package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import kotlin.time.Duration.Companion.hours

internal class TrendingBooksCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        val refreshSettled = MutableStateFlow(value = false)

        coroutineScope {
            launch {
                dependencies.refreshTrendingBooksUseCase(
                    period = TrendingPeriod.Week,
                    maxAge = TRENDING_MAX_AGE,
                )
                    .onFailure { error ->
                        AppLog.e(
                            error,
                            "Failed to fetch trending books",
                        )
                    }

                refreshSettled.value = true
            }

            combine(
                dependencies.observeTrendingBooksUseCase(period = TrendingPeriod.Week),
                dependencies.observeTrendingFetchedAtUseCase(period = TrendingPeriod.Week),
                refreshSettled,
            ) { trendingBooks, fetchedAt, settled ->
                trendingBooks to (fetchedAt == null && settled.not())
            }.collect { (trendingBooks, loading) ->
                scope.setState {
                    it.copy(
                        trendingBooks = trendingBooks.toImmutableList(),
                        loadingTrendingBooks = loading,
                    )
                }
            }
        }
    }

    private companion object {
        val TRENDING_MAX_AGE = 24.hours
    }
}
