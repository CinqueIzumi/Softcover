package nl.rhaydus.softcover.feature.explore.presentation.action

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import timber.log.Timber

data object OnRefreshAction : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        scope.setState { it.copy(isRefreshing = true) }

        dependencies.continueSeriesRefreshTrigger.update { it + 1 }

        dependencies.getTrendingBooksUseCase()
            .onSuccess { trending ->
                val allUserBooks = dependencies.getAllUserBooksUseCase().first()

                val overlaid = trending.map { book ->
                    allUserBooks.find { it.id == book.id } ?: book
                }

                scope.setState { it.copy(trendingBooks = overlaid) }
            }
            .onFailure { error ->
                Timber.e(
                    error,
                    "Failed to refresh trending books",
                )
            }

        scope.setState { it.copy(isRefreshing = false) }
    }
}
