package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.feature.settings.domain.repository.RoadmapRepository

/**
 * Re-fetches the published roadmap. [force] bypasses the repository's cache TTL - the on-launch
 * refresh passes `false` (so opening the screen twice in an afternoon costs one request), while
 * pull-to-refresh and the error-slot retry pass `true`.
 */
class RefreshRoadmapUseCase(
    private val roadmapRepository: RoadmapRepository,
) {
    suspend operator fun invoke(force: Boolean = false): Result<Unit> =
        runCatchingLogged(context = "Failed to refresh the roadmap") {
            roadmapRepository.refreshRoadmap(force = force)
        }
}
