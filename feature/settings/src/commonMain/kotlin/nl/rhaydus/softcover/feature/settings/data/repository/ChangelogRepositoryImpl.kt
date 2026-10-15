package nl.rhaydus.softcover.feature.settings.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentKey
import nl.rhaydus.softcover.feature.settings.data.mapper.toChangelog
import nl.rhaydus.softcover.feature.settings.domain.model.Changelog
import nl.rhaydus.softcover.feature.settings.domain.repository.ChangelogRepository

internal class ChangelogRepositoryImpl(
    private val documentCache: PublishedDocumentCache,
) : ChangelogRepository {
    override fun observeChangelog(): Flow<Changelog> =
        documentCache.observe(key = DOCUMENT_KEY).map { it.toChangelog() }

    override suspend fun refreshChangelog(force: Boolean) {
        documentCache.refresh(
            key = DOCUMENT_KEY,
            force = force,
        )
    }

    private companion object {
        val DOCUMENT_KEY = PublishedDocumentKey.CHANGELOG
    }
}
