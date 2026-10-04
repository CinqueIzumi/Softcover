package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_FEATURED
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the featured upcoming release to its cover UI model off the composition, deriving off
 * [ActionScope.state] rather than being patched into each writer.
 */
internal class CoverModelsCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        scope.state
            .map { it.featuredUpcomingRelease }
            .distinctUntilChanged()
            .collectLatest { featured ->
                scope.setState {
                    it.copy(
                        featuredCover = featured?.toCoverUiModel(
                            variant = CoverVariant.Raised,
                            shareTransition = true,
                            transitionSurface = SURFACE_FEATURED,
                        ),
                    )
                }
            }
    }
}
