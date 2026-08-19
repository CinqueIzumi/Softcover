package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.settings.presentation.event.RoadmapEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.RoadmapDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
import nl.rhaydus.toad.ActionScope

private const val LOAD_FAILURE_MESSAGE = "Couldn't load the roadmap. Pull to refresh to try again."

/**
 * The screen's single source of rendered content: the repository emits the cached copy (or the
 * bundled fallback when there is no cache yet) immediately, then re-emits after any successful
 * refresh. The first emission is what clears `isLoading` - there is always one, so the spinner is
 * only ever on screen for the first frame.
 *
 * The flow is guarded because the repository throws rather than degrading a failed read (the layered
 * error model): an unreadable cache row or bundled resource would otherwise take the screen model's
 * coroutine down. There is no `Result` to fold on a flow, so the failure is folded into the same
 * error slot a use-case failure would fill, and a retry re-fetches through
 * [RefreshRoadmapAction][nl.rhaydus.softcover.feature.settings.presentation.action.RefreshRoadmapAction].
 */
internal class RoadmapDocumentCollector : RoadmapCollector {
    override suspend fun onLaunch(
        scope: ActionScope<RoadmapUiState, RoadmapEvent, RoadmapLocalVariables>,
        dependencies: RoadmapDependencies,
    ) {
        dependencies.observeRoadmapUseCase()
            .catch { error ->
                AppLog.e(
                    error,
                    "Failed to read the roadmap document",
                )

                scope.setState {
                    it.copy(
                        isLoading = false,
                        roadmapError = LOAD_FAILURE_MESSAGE,
                    )
                }
            }
            .collectLatest { document ->
                scope.setState {
                    it.copy(
                        document = document,
                        isLoading = false,
                    )
                }
            }
    }
}
