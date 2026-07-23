package nl.rhaydus.softcover.feature.explore.presentation.action

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
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
        // Deleting the last character is a reset, not a search for the empty string: it hands the
        // screen back to the feed, keeping the user's cursor where it is (they are mid-edit).
        if (newQuery.isEmpty()) {
            resetSearch(
                dependencies = dependencies,
                scope = scope,
            )

            return
        }

        scope.setState {
            it.copy(
                searchText = newQuery,
                isLoading = true,
                searchError = null,
                // A keystroke always supersedes a mood browse, whether it starts a new text
                // search or (on the empty string above) returns to the feed.
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
