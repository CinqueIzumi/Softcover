package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.topbar.SearchTopBarUiModel
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreSearchPhase
import nl.rhaydus.toad.ActionScope

/**
 * Derives the search chrome's UI model off the composition (`component-contract.md` § 7.2 R9).
 *
 * Deciding that "active" means *any* phase other than the resting feed is a reading of the screen's
 * own state, not something the chrome should re-derive on every frame — and the four fields it reads
 * are written by a dozen different actions, so deriving off [ActionScope.state] rather than patching
 * each writer is what keeps the chrome from going stale (§ 5i).
 */
internal class SearchTopBarCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        scope.state
            .map {
                SearchTopBarUiModel(
                    query = it.searchText,
                    active = it.searchPhase != ExploreSearchPhase.FEED,
                    focused = it.searchFocused,
                    isLoading = it.isLoading,
                )
            }
            .distinctUntilChanged()
            .collectLatest { model ->
                scope.setState { it.copy(searchTopBar = model) }
            }
    }
}
