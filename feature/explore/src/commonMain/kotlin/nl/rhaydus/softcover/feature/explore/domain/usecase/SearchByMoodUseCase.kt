package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class SearchByMoodUseCase(
    private val exploreRepository: ExploreRepository,
) {
    /** [page] is 1-based, same replace/append contract as [SearchForNameUseCase]. */
    suspend operator fun invoke(
        mood: MoodTag,
        page: Int = 1,
    ): Result<Unit> = runCatchingLogged {
        exploreRepository.searchByMood(
            mood = mood,
            page = page,
        )
    }
}
