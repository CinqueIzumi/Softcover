package nl.rhaydus.softcover.feature.explore.presentation.action

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal data class OnQueryChangeAction(
    val newQuery: String,
    val searchDelay: Duration = 1.seconds,
) : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        scope.setState {
            it.copy(
                searchText = newQuery,
                isLoading = true,
                searchError = null,
                // A keystroke always supersedes a mood browse, whether it starts a new text
                // search or (on the empty string below) returns to the feed.
                activeMoodFilter = null,
                queriedBooksHasMore = true,
            )
        }

        scope.currentLocalVariables.queryJob?.cancelAndJoin()

        scope.setLocalVariables {
            it.copy(
                queryJob = null,
                searchResultsPage = 1,
            )
        }

        if (newQuery.isEmpty()) {
            scope.setState {
                it.copy(
                    queriedBooks = emptyList(),
                    isLoading = false,
                    searchError = null,
                )
            }

            // Also resets the data source's own queried-books state, not just the UiState mirror
            // above - otherwise re-running an identical search later (e.g. re-tapping the same
            // mood) can recompute an equal list that a StateFlow dedups, and the results silently
            // never reach the UI.
            dependencies.clearSearchResultsUseCase().onFailure {
                AppLog.e("$it")
            }

            return
        }

        val newSearchJob: Job = dependencies.launch {
            delay(searchDelay)

            executeSearch(
                dependencies = dependencies,
                scope = scope,
            )
        }

        scope.setLocalVariables {
            it.copy(queryJob = newSearchJob)
        }
    }
}
