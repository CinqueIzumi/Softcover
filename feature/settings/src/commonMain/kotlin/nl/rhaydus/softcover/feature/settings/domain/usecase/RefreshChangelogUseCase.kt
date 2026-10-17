package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.feature.settings.domain.repository.ChangelogRepository

/** Re-fetches the published changelog; [force] bypasses the repository's cache TTL. */
class RefreshChangelogUseCase(
    private val changelogRepository: ChangelogRepository,
) {
    suspend operator fun invoke(force: Boolean = false): Result<Unit> =
        runCatchingLogged(context = "Failed to refresh the changelog") {
            changelogRepository.refreshChangelog(force = force)
        }
}
