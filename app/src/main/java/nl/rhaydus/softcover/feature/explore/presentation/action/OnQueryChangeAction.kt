package nl.rhaydus.softcover.feature.explore.presentation.action

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class OnQueryChangeAction(
    val newQuery: String,
    val searchDelay: Duration = 1.seconds,
) :
 ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        scope.setState {
            it.copy(
                searchText = newQuery,
                isLoading = true,
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
                )
            }

            return
        }

        val newTimberJob: Job = dependencies.launch {
            delay(searchDelay)

            dependencies.searchForNameUseCase(name = scope.currentState.searchText).onFailure {
                Timber.e("$it")
            }

            scope.setState {
                it.copy(isLoading = false)
            }
        }

        scope.setLocalVariables {
            it.copy(queryJob = newTimberJob)
        }
    }
}