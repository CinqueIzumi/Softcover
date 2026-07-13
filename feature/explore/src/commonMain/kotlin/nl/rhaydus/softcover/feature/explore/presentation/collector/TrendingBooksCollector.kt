package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal class TrendingBooksCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        val trendingBooks: List<Book> = dependencies.getTrendingBooksUseCase()
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Failed to fetch trending books",
                )

                scope.setState { it.copy(loadingTrendingBooks = false) }
            }
            .getOrNull() ?: return

        dependencies.getAllUserBooksUseCase().collectLatest { allUserBooks ->
            val overlaid = trendingBooks.map { trending ->
                allUserBooks.find { it.id == trending.id } ?: trending
            }

            scope.setState {
                it.copy(
                    trendingBooks = overlaid,
                    loadingTrendingBooks = false,
                )
            }
        }
    }
}
