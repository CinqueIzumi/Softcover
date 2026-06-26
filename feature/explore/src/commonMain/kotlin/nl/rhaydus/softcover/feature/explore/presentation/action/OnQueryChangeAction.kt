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
        scope.setState {
            it.copy(
                searchText = newQuery,
                isLoading = true,
                searchError = null,
            )
        }

        scope.currentLocalVariables.queryJob?.cancelAndJoin()

        scope.setLocalVariables {
            it.copy(queryJob = null)
        }

        if (newQuery.isEmpty()) {
            scope.setState {
                it.copy(
                    queriedBooks = emptyList(),
                    isLoading = false,
                    searchError = null,
                )
            }

            return
        }

        val newSearchJob: Job = dependencies.launch {
            delay(searchDelay)

            executeBookSearch(
                name = scope.currentState.searchText,
                dependencies = dependencies,
                scope = scope,
            )
        }

        scope.setLocalVariables {
            it.copy(queryJob = newSearchJob)
        }
    }
}
