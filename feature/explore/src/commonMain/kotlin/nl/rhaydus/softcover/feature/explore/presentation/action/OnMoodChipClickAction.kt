package nl.rhaydus.softcover.feature.explore.presentation.action

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Fired from either the feed's mood grid or the search-focus "try a mood" chips - both surfaces
 * feed into the same results state (see [ExploreScreenUiState.searchPhase]'s modelling note).
 *
 * Seeding vs. a keystroke (explore-3a feedback item 6): [ExploreScreenUiState.searchText] is set
 * to the tapped mood's label directly, in this action's own [ActionScope.setState] call, so the
 * search field shows what the user tapped. This deliberately bypasses `OnQueryChangeAction` - the
 * *only* action that clears [ExploreScreenUiState.activeMoodFilter] on a text change, since it is
 * the sole entry point for a genuine user keystroke (wired to the search field's `onValueChange`).
 * Because this action never calls `OnQueryChangeAction`, populating the field here can never
 * self-clear the very filter it just set; a real edit still reaches `OnQueryChangeAction` and
 * clears the filter as before.
 */
internal data class OnMoodChipClickAction(val mood: MoodTag) : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        scope.currentLocalVariables.queryJob?.cancelAndJoin()

        scope.setLocalVariables {
            it.copy(
                queryJob = null,
                searchResultsPage = 1,
            )
        }

        scope.setState {
            it.copy(
                searchFocused = false,
                activeMoodFilter = mood,
                searchText = mood.label,
                isLoading = true,
                searchError = null,
                queriedBooksHasMore = true,
            )
        }

        val searchJob: Job = dependencies.launch {
            executeSearch(
                dependencies = dependencies,
                scope = scope,
            )
        }

        scope.setLocalVariables {
            it.copy(queryJob = searchJob)
        }
    }
}
