package nl.rhaydus.softcover.feature.explore.presentation.collector

import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Runs the backfill for legacy hidden rows when the Explore shelf loads, mirroring the Hidden
 * Suggestions screen's [EnrichMetadataCollector]. It runs here as well because a row missing its
 * series cursor holds its whole series out of "up next" - waiting for the user to open Hidden
 * Suggestions would leave that series silently empty indefinitely. Once the backfill writes the
 * cursor, the dismissed-books flow re-emits and the continue-series seeds recompute on their own.
 */
internal class EnrichDismissedMetadataCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        dependencies.enrichDismissedContinueSeriesMetadataUseCase()
    }
}
