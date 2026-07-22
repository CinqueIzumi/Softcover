package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal class FeaturedUpcomingReleaseCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        val featured: Book? = dependencies.getFeaturedUpcomingReleaseUseCase()
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Failed to fetch featured upcoming release",
                )
            }
            .getOrNull()

        if (featured == null) {
            scope.setState { it.copy(loadingFeaturedUpcomingRelease = false) }
            return
        }

        dependencies.getAllUserBooksUseCase().collectLatest { allUserBooks ->
            scope.setState {
                it.copy(
                    featuredUpcomingRelease = allUserBooks.find { book -> book.id == featured.id } ?: featured,
                    loadingFeaturedUpcomingRelease = false,
                )
            }
        }
    }
}
