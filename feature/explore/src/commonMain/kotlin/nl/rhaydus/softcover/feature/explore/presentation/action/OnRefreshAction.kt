package nl.rhaydus.softcover.feature.explore.presentation.action

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import kotlin.time.Duration

internal data object OnRefreshAction : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        scope.setState { it.copy(isRefreshing = true) }

        dependencies.continueSeriesRefreshTrigger.update { it + 1 }

        coroutineScope {
            launch {
                dependencies.refreshTrendingBooksUseCase(
                    period = TrendingPeriod.Week,
                    maxAge = Duration.ZERO,
                )
                    .onFailure { error ->
                        AppLog.e(
                            error,
                            "Failed to refresh trending books",
                        )
                    }
            }

            launch {
                dependencies.refreshRecommendedBooksUseCase(maxAge = Duration.ZERO)
                    .onFailure { error ->
                        AppLog.e(
                            error,
                            "Failed to refresh recommended books",
                        )
                    }
            }
        }

        scope.setState { it.copy(isRefreshing = false) }
    }
}
