package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.error.toUserMessage
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Runs whichever search is currently active - a mood filter, if [ExploreScreenUiState.activeMoodFilter]
 * is set, otherwise a text search over [ExploreScreenUiState.searchText] - and folds the result
 * into state. Shared by every action that starts, retries, re-sorts, or pages a search, so the
 * fetch/fold logic lives in exactly one place. [page] is 1-based: 1 is a fresh search/replace,
 * anything higher appends (see `SearchRemoteDataSource`'s paging contract).
 */
internal suspend fun executeSearch(
    dependencies: ExploreDependencies,
    scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    page: Int = 1,
) {
    val state = scope.currentState
    val mood = state.activeMoodFilter

    val result = if (mood != null) {
        dependencies.searchByMoodUseCase(
            mood = mood,
            page = page,
        )
    } else {
        dependencies.searchForNameUseCase(
            name = state.searchText,
            sortMode = state.sortMode,
            page = page,
        )
    }

    result
        .onSuccess {
            scope.setState {
                it.copy(
                    isLoading = false,
                    loadingMoreQueriedBooks = false,
                )
            }
        }
        .onFailure { error ->
            scope.setState {
                it.copy(
                    isLoading = false,
                    loadingMoreQueriedBooks = false,
                    searchError = error.toUserMessage() ?: "We couldn't load results. Please try again.",
                )
            }
        }
}
