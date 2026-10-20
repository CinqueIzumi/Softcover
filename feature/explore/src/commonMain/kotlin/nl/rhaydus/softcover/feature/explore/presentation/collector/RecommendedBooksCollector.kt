package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import kotlin.time.Duration.Companion.hours

internal class RecommendedBooksCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        val refreshSettled = MutableStateFlow(value = false)

        coroutineScope {
            launch {
                dependencies.refreshRecommendedBooksUseCase(maxAge = RECOMMENDED_MAX_AGE)
                    .onFailure { error ->
                        AppLog.e(
                            error,
                            "Failed to fetch recommended books",
                        )
                    }

                refreshSettled.value = true
            }

            combine(
                dependencies.getRecommendedBooksUseCase(),
                dependencies.observeRecommendedFetchedAtUseCase(),
                refreshSettled,
            ) { recommendedBooks, fetchedAt, settled ->
                recommendedBooks to (fetchedAt == null && settled.not())
            }.collect { (recommendedBooks, loading) ->
                scope.setState {
                    it.copy(
                        recommendedBooks = recommendedBooks.toImmutableList(),
                        loadingRecommendedBooks = loading,
                    )
                }
            }
        }
    }

    private companion object {
        val RECOMMENDED_MAX_AGE = 1.hours
    }
}
