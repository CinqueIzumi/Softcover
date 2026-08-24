package nl.rhaydus.softcover.feature.explore.presentation.action

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal data object OnRefreshAction : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        scope.setState { it.copy(isRefreshing = true) }

        dependencies.continueSeriesRefreshTrigger.update { it + 1 }

        // forceRefresh: this is a pull-to-refresh, so it must bypass the session cache that the
        // on-mount collector reads from - otherwise the gesture spins and changes nothing.
        dependencies.getTrendingBooksUseCase(forceRefresh = true)
            .onSuccess { trending ->
                // Nothing catches a throw between here and the screen model's scope, and a terminal read
                // re-throws an upstream failure even as `firstOrNull()`. No overlay is the safe default.
                val allUserBooks = runCatchingCancellable {
                    dependencies.getAllUserBooksUseCase().firstOrNull()
                }.getOrNull().orEmpty()

                val overlaid = trending.map { book ->
                    allUserBooks.find { it.id == book.id } ?: book
                }

                scope.setState { it.copy(trendingBooks = overlaid) }
            }
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Failed to refresh trending books",
                )
            }

        scope.setState { it.copy(isRefreshing = false) }
    }
}
