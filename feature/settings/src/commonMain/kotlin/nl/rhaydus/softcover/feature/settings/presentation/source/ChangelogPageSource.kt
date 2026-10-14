package nl.rhaydus.softcover.feature.settings.presentation.source

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocumentSource
import nl.rhaydus.softcover.feature.settings.domain.usecase.ObserveChangelogUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.RefreshChangelogUseCase
import nl.rhaydus.softcover.feature.settings.presentation.mapper.toDocumentSection
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPageSnapshot

/** The changelog maps to one section per release, the installed one marked as the reader's own. */
internal class ChangelogPageSource(
    private val observeChangelogUseCase: ObserveChangelogUseCase,
    private val refreshChangelogUseCase: RefreshChangelogUseCase,
    private val appVersionProvider: AppVersionProvider,
) : DocumentPageSource {
    override fun observe(): Flow<DocumentPageSnapshot> {
        val installedVersion = appVersionProvider.versionInfo.name

        return observeChangelogUseCase().map { changelog ->
            DocumentPageSnapshot(
                sections = changelog.entries
                    .map { entry -> entry.toDocumentSection(installedVersion = installedVersion) }
                    .toImmutableList(),
                isBundled = changelog.source == PublishedDocumentSource.BUNDLED,
                fetchedAtEpochMillis = changelog.fetchedAtEpochMillis,
            )
        }
    }

    override suspend fun refresh(force: Boolean): Result<Unit> = refreshChangelogUseCase(force = force)
}
