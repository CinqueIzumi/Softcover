package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.MOOD_STAND_IN_TILE
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toMoodTile
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps [ExploreScreenUiState.moodTags] to [ExploreScreenUiState.moodTiles] off the composition, with four
 * masked stand-ins while the first load has no tags yet.
 */
internal class MoodTilesCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        scope.state
            .map { state -> state.moodTags to state.loadingMoodTags }
            .distinctUntilChanged()
            .collectLatest { (moodTags, isLoading) ->
                val tiles = if (isLoading && moodTags.isEmpty()) {
                    List(size = 4) { MOOD_STAND_IN_TILE }.toImmutableList()
                } else {
                    moodTags.mapIndexed { index, mood -> mood.toMoodTile(index = index) }.toImmutableList()
                }

                scope.setState { it.copy(moodTiles = tiles) }
            }
    }
}
