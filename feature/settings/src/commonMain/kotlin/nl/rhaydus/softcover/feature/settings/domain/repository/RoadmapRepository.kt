package nl.rhaydus.softcover.feature.settings.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument

interface RoadmapRepository {
    fun observeRoadmap(): Flow<RoadmapDocument>

    suspend fun refreshRoadmap(force: Boolean)
}
