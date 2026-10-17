package nl.rhaydus.softcover.feature.settings.data.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentKey
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocument
import nl.rhaydus.softcover.feature.settings.domain.repository.RoadmapRepository

internal class RoadmapRepositoryImpl(
    private val documentCache: PublishedDocumentCache,
) : RoadmapRepository {
    override fun observeRoadmap(): Flow<PublishedDocument> = documentCache.observe(key = DOCUMENT_KEY)

    override suspend fun refreshRoadmap(force: Boolean) {
        documentCache.refresh(
            key = DOCUMENT_KEY,
            force = force,
        )
    }

    private companion object {
        val DOCUMENT_KEY = PublishedDocumentKey.ROADMAP
    }
}
