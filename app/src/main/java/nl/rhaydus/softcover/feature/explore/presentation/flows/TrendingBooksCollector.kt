package nl.rhaydus.softcover.feature.explore.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.domain.model.Book
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
        val trendingBooks: List<Book> = dependencies.getTrendingBooksUseCase()
            .onFailure { error ->
                Timber.e(error, "Failed to fetch trending books")

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
