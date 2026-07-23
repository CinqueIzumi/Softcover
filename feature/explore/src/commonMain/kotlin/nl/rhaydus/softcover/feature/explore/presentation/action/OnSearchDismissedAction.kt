package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * The user let go of search - a tap outside the focus surface, or the field losing focus. Only the
 * chrome's state is touched: any query and its results survive, so dismissing while results are on
 * screen puts the keyboard away without throwing the results away (that is [OnClearSearchAction]).
 */
internal data object OnSearchDismissedAction : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        scope.setState { it.copy(searchFocused = false) }
    }
}
