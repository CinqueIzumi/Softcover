package nl.rhaydus.softcover.feature.settings.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocument

interface RoadmapRepository {
    fun observeRoadmap(): Flow<PublishedDocument>

    suspend fun refreshRoadmap(force: Boolean)
}
