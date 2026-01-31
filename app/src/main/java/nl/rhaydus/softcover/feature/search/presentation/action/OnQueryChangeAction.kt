package nl.rhaydus.softcover.feature.search.presentation.action

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState
import nl.rhaydus.softcover.feature.search.presentation.viewmodel.SearchDependencies
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class OnQueryChangeAction(
    val newQuery: String,
    val searchDelay: Duration = 1.seconds,
) :
    SearchAction {
    override suspend fun execute(
        dependencies: SearchDependencies,
        scope: ActionScope<SearchScreenUiState, SearchEvent, SearchLocalVariables>,
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
            delay(1.seconds)

            dependencies.searchForNameUseCase(name = scope.currentState.searchText).onFailure {
                Timber.e("-=- $it")
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