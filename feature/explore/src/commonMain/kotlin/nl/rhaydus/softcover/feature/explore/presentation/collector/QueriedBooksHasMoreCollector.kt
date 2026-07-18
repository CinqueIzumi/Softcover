package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Kept separate from [QueriedBooksCollector] rather than combined into its flow, so a search-paging
 * change never touches the (already well-covered) merge-with-owned-books collector or its tests.
 */
internal class QueriedBooksHasMoreCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        dependencies.getQueriedBooksHasMoreUseCase().collectLatest { hasMore ->
            scope.setState { it.copy(queriedBooksHasMore = hasMore) }
        }
    }
}
