package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

// The session cache that keeps this to one request per session lives in the repository, which is
// what decides whether a read is answered locally or from the network - see
// ExploreRepository.fetchFeaturedUpcomingRelease.
class GetFeaturedUpcomingReleaseUseCase(
    private val exploreRepository: ExploreRepository,
) {
    suspend operator fun invoke(): Result<Book?> = runCatchingLogged {
        exploreRepository.fetchFeaturedUpcomingRelease()
    }
}
