package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * The user reached for search - a tap anywhere on the search pill, or the field gaining focus some
 * other way (a desktop Tab key). Level-triggered on purpose: it is dispatched on *every* tap, not
 * only when the platform field changes focus state, so the focus surface stays reachable even if
 * the field is already holding focus. Its counterpart is [OnSearchDismissedAction].
 */
internal data object OnSearchActivatedAction : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        scope.setState { it.copy(searchFocused = true) }
    }
}
