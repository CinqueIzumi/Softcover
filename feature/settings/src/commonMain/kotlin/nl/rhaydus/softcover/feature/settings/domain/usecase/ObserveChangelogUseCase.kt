package nl.rhaydus.softcover.feature.settings.domain.usecase

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.core.domain.app.VersionComparator
import nl.rhaydus.softcover.feature.settings.domain.model.Changelog
import nl.rhaydus.softcover.feature.settings.domain.repository.ChangelogRepository

/** Emits the changelog without the releases newer than the installed app, which the reader does not have yet. */
class ObserveChangelogUseCase(
    private val changelogRepository: ChangelogRepository,
    private val appVersionProvider: AppVersionProvider,
) {
    operator fun invoke(): Flow<Changelog> = changelogRepository.observeChangelog().map { changelog ->
        val installedVersion = appVersionProvider.versionInfo.name

        changelog.copy(
            entries = changelog.entries
                .filter { entry ->
                    VersionComparator.isNewer(
                        candidate = entry.version,
                        current = installedVersion,
                    ).not()
                }
                .toImmutableList(),
        )
    }
}
