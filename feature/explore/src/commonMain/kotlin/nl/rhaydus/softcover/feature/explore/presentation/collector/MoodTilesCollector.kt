package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/** Maps [ExploreScreenUiState.moodTags] to [ExploreScreenUiState.moodTiles] off the composition. */
internal class MoodTilesCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        scope.state
            .map { state -> MoodTilesSnapshot(moodTags = state.moodTags) }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val tiles = snapshot.compute()

                scope.setState { it.copy(moodTiles = tiles) }
            }
    }
}
