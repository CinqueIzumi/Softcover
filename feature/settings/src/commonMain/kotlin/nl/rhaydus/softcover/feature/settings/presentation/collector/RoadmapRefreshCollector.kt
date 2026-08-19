package nl.rhaydus.softcover.feature.settings.presentation.collector

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.settings.presentation.event.RoadmapEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.RoadmapDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
import nl.rhaydus.toad.ActionScope

/**
 * Pulls a fresh copy when the screen opens, subject to the repository's cache TTL — so a reader who
 * opens the roadmap twice in an afternoon costs one request, not two. The failure is logged but not
 * surfaced: the reader still has the cached or bundled copy on screen, and an error banner over
 * readable content would be noise. A failure the reader asked for — the pull-to-refresh in
 * [RefreshRoadmapAction][nl.rhaydus.softcover.feature.settings.presentation.action.RefreshRoadmapAction]
 * — does fill the error slot, because there the request was explicit.
 */
internal class RoadmapRefreshCollector : RoadmapCollector {
    override suspend fun onLaunch(
        scope: ActionScope<RoadmapUiState, RoadmapEvent, RoadmapLocalVariables>,
        dependencies: RoadmapDependencies,
    ) {
        dependencies.refreshRoadmapUseCase(force = false).onFailure { error ->
            AppLog.e(
                error,
                "Failed to refresh the roadmap on launch",
            )
        }
    }
}
