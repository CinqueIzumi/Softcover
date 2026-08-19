package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.error.toUserMessage
import nl.rhaydus.softcover.feature.settings.presentation.event.RoadmapEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.RoadmapDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
import nl.rhaydus.toad.ActionScope

private const val REFRESH_FAILURE_MESSAGE = "Couldn't reach the roadmap. Check your connection and try again."

/**
 * Re-fetches the published roadmap, bypassing the cache TTL — this is what both pull-to-refresh and
 * the error slot's retry dispatch, since a retry is just re-running the screen's own action rather
 * than a separate "retry" type. The document itself is not set here: the write lands in the cache and
 * [RoadmapDocumentCollector][nl.rhaydus.softcover.feature.settings.presentation.collector.RoadmapDocumentCollector]
 * re-emits it, so there is exactly one path by which the rendered document changes.
 */
internal class RefreshRoadmapAction : RoadmapAction {
    override suspend fun execute(
        dependencies: RoadmapDependencies,
        scope: ActionScope<RoadmapUiState, RoadmapEvent, RoadmapLocalVariables>,
    ) {
        if (scope.currentState.isRefreshing) return

        scope.setState {
            it.copy(
                isRefreshing = true,
                roadmapError = null,
            )
        }

        dependencies.refreshRoadmapUseCase(force = true)
            .onFailure { error ->
                val message = error.toUserMessage() ?: REFRESH_FAILURE_MESSAGE

                scope.setState { it.copy(roadmapError = message) }
            }

        scope.setState { it.copy(isRefreshing = false) }
    }
}
