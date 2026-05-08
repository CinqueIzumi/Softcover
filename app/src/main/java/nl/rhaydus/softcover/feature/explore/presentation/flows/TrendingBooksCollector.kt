package nl.rhaydus.softcover.feature.explore.presentation.flows

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import timber.log.Timber

class TrendingBooksCollector : ExploreInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        dependencies.getTrendingBooksUseCase()
            .onSuccess { books ->
                scope.setState {
                    it.copy(
                        trendingBooks = books,
                        loadingTrendingBooks = false,
                    )
                }
            }
            .onFailure { error ->
                Timber.e(error, "Failed to fetch trending books")

                scope.setState { it.copy(loadingTrendingBooks = false) }
            }
    }
}
