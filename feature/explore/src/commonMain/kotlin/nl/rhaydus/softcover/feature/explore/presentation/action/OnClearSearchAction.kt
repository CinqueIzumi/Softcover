package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * The search chrome's clear (×): leave search entirely - drop the query, the mood browse, and the
 * results, and collapse the chrome back to the resting feed pill. The chrome state goes first so
 * the pill and keyboard let go on the same frame as the tap, rather than waiting on the in-flight
 * fetch that [resetSearch] cancels.
 *
 * Dropping [ExploreScreenUiState.searchFocused] here is a *state* change, and the search chrome
 * follows state: the top bar clears the platform focus and hides the keyboard off the back of it.
 * The component must never be handed a "clear" that unfocuses the field behind the screen's back -
 * a field left holding focus while state says otherwise is exactly the desync that strands the
 * cursor on an unopenable search surface.
 */
internal data object OnClearSearchAction : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        scope.setState { it.copy(searchFocused = false) }

        resetSearch(
            dependencies = dependencies,
            scope = scope,
        )
    }
}
