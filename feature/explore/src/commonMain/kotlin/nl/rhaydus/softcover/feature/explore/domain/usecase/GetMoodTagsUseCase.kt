package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class GetMoodTagsUseCase(
    private val exploreRepository: ExploreRepository,
) {
    suspend operator fun invoke(): Result<List<MoodTag>> = runCatchingLogged {
        exploreRepository.fetchMoodTags()
    }
}
