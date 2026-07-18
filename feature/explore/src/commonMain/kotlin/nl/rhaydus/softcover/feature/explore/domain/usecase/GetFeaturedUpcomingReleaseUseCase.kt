package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class GetFeaturedUpcomingReleaseUseCase(
    private val exploreRepository: ExploreRepository,
) {
    suspend operator fun invoke(): Result<Book?> = runCatchingLogged {
        exploreRepository.fetchFeaturedUpcomingRelease()
    }
}
