package nl.rhaydus.softcover.feature.settings.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocument
import nl.rhaydus.softcover.feature.settings.domain.repository.RoadmapRepository

class ObserveRoadmapUseCase(
    private val roadmapRepository: RoadmapRepository,
) {
    operator fun invoke(): Flow<PublishedDocument> = roadmapRepository.observeRoadmap()
}
